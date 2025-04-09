package net.detalk.api.geo.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.admin.controller.v1.response.GetVisitorLogResponse;
import net.detalk.api.admin.domain.VisitorLog;
import net.detalk.api.admin.domain.exception.VisitorLocationSaveException;
import net.detalk.api.admin.repository.VisitorLogRepository;
import net.detalk.api.geo.domain.GeoInfo;
import net.detalk.api.support.util.EnvironmentHolder;
import net.detalk.api.support.paging.PagingData;
import net.detalk.api.support.util.TimeHolder;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpLookupService {

    private final VisitorLogRepository visitorLogRepository;
    private final TimeHolder timeHolder;
    private final EnvironmentHolder env;

    // GeoLite2-City.mmdb 기반 DatabaseReader
    private final DatabaseReader databaseReader;

    /**
     * 클라이언트 IP, 세션 ID, User-Agent, Referer 정보를 받아 방문자 정보 저장
     *
     * @param clientIp  클라이언트 IP 주소
     * @param sessionId 클라이언트의 세션 ID
     * @param userAgent 브라우저 정보
     * @param referer   이전 페이지 정보
     */
    @Async("visitorLogTaskExecutor")
    public void saveVisitorLocation(String clientIp, String sessionId, String userAgent,
        String referer) {

        try {

            String continentCode;
            String countryIso;
            String countryName;
            LocalDateTime now = timeHolder.dateTime();

            if ("dev".equals(env.getActiveProfile())) {
                // 개발 환경일 경우
                continentCode = "AS";
                countryIso = "KR";
                countryName = "LOCAL_KOREA";
            } else {
                // 개발환경 아닐 경우
                InetAddress ipAddress = InetAddress.getByName(clientIp);
                CityResponse cityResponse = databaseReader.city(ipAddress);
                continentCode = cityResponse.getContinent().getCode();
                countryIso = cityResponse.getCountry().getIsoCode();
                countryName = cityResponse.getRegisteredCountry().getName();
            }

            MDC.put("continentCode", continentCode);
            MDC.put("countryIso", countryIso);
            MDC.put("countryName", countryName);
            MDC.put("clientIp", clientIp);
            MDC.put("userAgent", userAgent);

            log.info("Visitor location information logged");

            VisitorLog visitorLog = VisitorLog.builder()
                .sessionId(sessionId)
                .continentCode(continentCode)
                .countryIso(countryIso)
                .countryName(countryName)
                .visitedAt(now)
                .userAgent(userAgent)
                .referer(referer)
                .build();

            visitorLogRepository.save(visitorLog);


        } catch (GeoIp2Exception e) {
            log.warn("GeoIP2 데이터베이스 조회 중 에러 발생 (sessionId: {})", sessionId);
            log.debug("GeoIP2 에러={}", e.getMessage());
            throw new VisitorLocationSaveException("사용자 위치 정보 저장 중 GeoLite2 DB 에러 발생");
        } catch (IOException e) {
            log.warn("사용자 위치 정보 저장 중 에러 발생 (sessionId: {})", sessionId);
            log.debug("사용자 위치 정보 에러={}", e.getMessage());
            throw new VisitorLocationSaveException("사용자 위치 정보 저장 중 IOE 에러 발생");
        }finally {
            MDC.clear(); // 비동기 스레드에서 독립적으로 실행되므로 MDC 정리 해야함
        }
    }

    /**
     * IP 주소를 기반으로 Geo 정보를 조회합니다.
     *
     * @param ipAddress 조회할 IP 주소 문자열
     * @return 국가, 도시 정보가 담긴 GeoInfo 객체 Optional, 조회 실패 시 Optional.empty()
     */
    public Optional<GeoInfo> lookupGeoInfo(String ipAddress) {
        // 유효하지 않은 IP 주소는 처리하지 않음
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            return Optional.empty();
        }


        // 개발 환경에서는 고정값 반환
        if ("dev".equals(env.getActiveProfile())) {
            ipAddress = "34.21.9.50";
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse cityResponse = databaseReader.city(inetAddress);

            if (cityResponse != null) {

                String continentCode = cityResponse.getContinent().getCode();
                String countryIso = cityResponse.getCountry().getIsoCode();
                String countryName = cityResponse.getRegisteredCountry().getName();
                String cityName = cityResponse.getCity().getName();
                System.out.println("continentCode = " + continentCode);
                System.out.println("countryIso = " + countryIso);
                System.out.println("countryName = " + countryName);
                System.out.println("cityName = " + cityName);

                return Optional.of(
                    GeoInfo.builder()
                        .continentCode(continentCode)
                        .countryIso(countryIso)
                        .countryName(countryName)
                        .cityName(cityName)
                        .build()
                );

            }
        } catch (GeoIp2Exception | IOException e) {
            log.warn("GeoIP lookup failed for IP {}: {}", ipAddress, e.getMessage());
            log.debug("GeoIP lookup error details", e);
        }
        return Optional.empty();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PagingData<GetVisitorLogResponse> findAll(Pageable pageable) {

        Page<VisitorLog> page = visitorLogRepository.findAll(pageable);

        var content = page.getContent().stream()
            .map(result -> new GetVisitorLogResponse(
                result.getSessionId(),
                result.getContinentCode(),
                result.getCountryIso(),
                result.getCountryName(),
                result.getVisitedAt(),
                result.getUserAgent(),
                result.getReferer()
            )).toList();

        return new PagingData<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

}
