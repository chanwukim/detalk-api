package net.detalk.api.admin.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.admin.controller.response.VisitorLogResponse;
import net.detalk.api.admin.domain.VisitorLog;
import net.detalk.api.admin.domain.exception.VisitorLocationSaveException;
import net.detalk.api.admin.repository.VisitorLogRepository;
import net.detalk.api.support.EnvironmentHolder;
import net.detalk.api.support.PagingData;
import net.detalk.api.support.TimeHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorLogService {

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
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PagingData<VisitorLogResponse> findAll(Pageable pageable) {

        Page<VisitorLog> page = visitorLogRepository.findAll(pageable);

        var content = page.getContent().stream()
            .map(result -> new VisitorLogResponse(
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
