package net.detalk.api.support.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 처리 Thread 설정 클래스
 * vCPU : 2, RAM 4GB (Swap 2GB)에 맞게 Thread 수를 적절하게 설정해야 한다.
 * 해당 클래스가 없다면 비동기 처리시 ThreadPool을 사용하지 않고 Thread를 계속 생산한다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "visitorLogTaskExecutor")
    public Executor visitorLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // 최소 스레드 수
        executor.setMaxPoolSize(3); // 최대 스레드 수
        executor.setQueueCapacity(50); // 요청 대기열 크기
        executor.setThreadNamePrefix("VisitorLog-");
        executor.initialize();
        return executor;
    }
}
