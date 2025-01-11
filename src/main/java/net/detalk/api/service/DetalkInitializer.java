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
        List<String> dbPlans = pricingPlanRepository.findAllNames();
        List<String> defaultPlans = getPlans();

        List<PricingPlan> planList = new ArrayList<>();

        for (String plan : defaultPlans) {
            if (!dbPlans.contains(plan)) {
                planList.add(
                    PricingPlan.builder()
                    .name(plan)
                    .build()
                );
            }
        }

        if (!planList.isEmpty()) {
            pricingPlanRepository.saveAll(planList);
            log.info("총 {}건의 가격 정책이 저장되었습니다. {}",planList.size(),planList);
        }

        log.info("가격 정책 초기화 완료.");
    }

    private List<String> getPlans() {
        return List.of("Free","Paid","Paid with free trial or plan");
    }

}
