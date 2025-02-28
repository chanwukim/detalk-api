package net.detalk.api.support.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // pricingPlan 캐시 커스텀 설정
        cacheManager.registerCustomCache("pricingPlan",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .initialCapacity(5)
                .maximumSize(20)
                .recordStats()
                .build());

        // 커스텀 설정 없으면 아래 값이 기본값으로 됨
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .initialCapacity(5)
            .maximumSize(50)
            .recordStats());

        return cacheManager;
    }

}