package net.detalk.api.service;


import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.repository.PricingPlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DetalkInitializer implements ApplicationRunner {

    private final PricingPlanRepository pricingPlanRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("가격 정책 초기화중...");
        log.info("가격 정책 초기화 완료.");
    }

}
