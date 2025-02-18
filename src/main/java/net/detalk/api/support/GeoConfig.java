package net.detalk.api.support;

import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
public class GeoConfig {

    @Value("${geo.database.path}")
    private String geoLite2CityDb;

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        log.info("GeoLite2 데이터베이스 초기화 시작: {}", geoLite2CityDb);
        ClassPathResource resource = new ClassPathResource(geoLite2CityDb);

        if (!resource.exists()) {
            log.error("GeoLite2 데이터베이스 파일을 찾을 수 없음: {}", geoLite2CityDb);
            throw new IllegalStateException("GeoLite2 데이터베이스 파일을 찾을 수 없습니다: " + geoLite2CityDb);
        }

        try {
            File database = resource.getFile();

            DatabaseReader reader = new DatabaseReader.Builder(database)
                // TODO : 메모리 공간에 직접 매핑하여 캐싱하는 방식 (추후 모니터링해서 자원 많이쓰면 취소)
                .fileMode(Reader.FileMode.MEMORY_MAPPED)
                .build();
            log.info("GeoLite2 데이터베이스 초기화 완료");
            return reader;
        } catch (IOException e) {
            log.error("GeoLite2 데이터베이스 로드 실패", e);
            throw new IllegalStateException("GeoLite2 데이터베이스 파일을 로드하는 중 오류가 발생했습니다", e);
        }
    }

}
