package net.detalk.api.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import java.net.InetAddress;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.VisitorLog;
import net.detalk.api.domain.exception.VisitorLocationSaveException;
import net.detalk.api.repository.VisitorLogRepository;
import net.detalk.api.support.EnvironmentHolder;
import net.detalk.api.support.TimeHolder;
import org.springframework.scheduling.annotation.Async;
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
    @Async
    public void saveVisitorLocation(String clientIp, String sessionId, String userAgent,
        String referer) {

        try{

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

        } catch (Exception e) {
            log.warn("사용자 위치 정보 저장 중 오류 발생 (clientIp: {})", clientIp);
            throw new VisitorLocationSaveException("방문자 위치 정보 저장에 실패했습니다.");
        }
    }
}
