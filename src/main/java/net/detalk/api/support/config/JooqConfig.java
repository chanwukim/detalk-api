package net.detalk.api.support.config;

import lombok.RequiredArgsConstructor;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.support.web.listener.PerformanceListener;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class JooqConfig {

    private final AlarmSender alarmSender;

    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return conf -> conf.set(performanceListener());  // 슬로우 쿼리 탐지를 위한 내 커스텀 리스너 추가
    }

    @Bean
    public PerformanceListener performanceListener() {
        return new PerformanceListener(alarmSender);
    }
}
