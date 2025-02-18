package net.detalk.api.support;

import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GeoConfig {

    private static final String GEO_LITE2_CITY_DB = "geolocate/GeoLite2-City.mmdb";

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        File database = new ClassPathResource(GEO_LITE2_CITY_DB).getFile();
        return new DatabaseReader.Builder(database).build();
    }

}
