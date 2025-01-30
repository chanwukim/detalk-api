package net.detalk.api.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.EnvironmentHolder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetalkInitializer implements ApplicationRunner {

    private final PricingPlanCache pricingPlanCache;

    private final DiscordService discordService;

    private final EnvironmentHolder env;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 가격 정책 캐싱
        pricingPlanCache.loadPricingPlans();

        // 트랜잭션과 무관한 Discord 연동 초기화
        initializeDiscord();

        if ("prod".equals(env.getActiveProfile())) {
            log.info("운영 서버 톰캣 실행 완료");
            discordService.sendMessage("운영 서버 톰캣 실행 완료");
        }
    }

    private void initializeDiscord() {
        try {
            discordService.initialize();
        } catch (Exception e) {
            log.error("Discord 봇 초기화 실패: {}", e.getMessage(), e);
        }
    }

}
