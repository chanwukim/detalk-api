package net.detalk.api.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.repository.PricingPlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetalkInitializer implements ApplicationRunner {

    private final PricingPlanRepository pricingPlanRepository;
    private final PricingPlanCache pricingPlanCache;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 가격 정책 캐싱
        pricingPlanCache.loadPricingPlans();
    }

}
