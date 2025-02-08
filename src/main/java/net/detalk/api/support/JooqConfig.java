package net.detalk.api.support;

import lombok.RequiredArgsConstructor;
import net.detalk.api.service.DiscordService;
import net.detalk.api.support.listener.PerformanceListener;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class JooqConfig {

    private final DiscordService discordService;

    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return conf -> {
            conf.set(new PerformanceListener(discordService));  // 슬로우 쿼리 탐지를 위한 내 커스텀 리스너 추가
        };
    }
}
