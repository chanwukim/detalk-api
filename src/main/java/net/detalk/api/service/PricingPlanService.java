package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.PricingPlan;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PricingPlanService {

    private final DetalkCache<String, PricingPlan> pricingPlanCache;

    public PricingPlan findByName(String name) {
        return pricingPlanCache.get(name);
    }
}
