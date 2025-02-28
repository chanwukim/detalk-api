package net.detalk.api.plan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.plan.domain.PricingPlan;
import net.detalk.api.plan.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.plan.repository.PricingPlanRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PricingPlanService {

    private final PricingPlanRepository pricingPlanRepository;

    @Cacheable(value = "pricingPlan", key = "#p0")
    public PricingPlan findByName(String name) {
        return pricingPlanRepository.findByName(name)
            .orElseThrow(() -> new PricingPlanNotFoundException(name));
    }

}
