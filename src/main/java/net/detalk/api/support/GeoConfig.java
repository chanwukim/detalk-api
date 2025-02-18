package net.detalk.api.support;

import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GeoConfig {

    @Value("${geo.database.path}")
    private String geoLite2CityDb;

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        ClassPathResource resource = new ClassPathResource(geoLite2CityDb);

        if (!resource.exists()) {
            throw new IllegalStateException("GeoLite2 데이터베이스 파일을 찾을 수 없습니다: " + geoLite2CityDb);
        }

        try {
            File database = resource.getFile();
            return new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new IllegalStateException("GeoLite2 데이터베이스 파일을 로드하는 중 오류가 발생했습니다", e);
        }
    }

}
