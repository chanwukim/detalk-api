package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.repository.PricingPlanRepository;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PricingPlanService {

    private final PricingPlanRepository pricingPlanRepository;

    public PricingPlan findById(String name) {
        return pricingPlanRepository.findByName(name).orElseThrow(() -> {
            log.error("[findById] 가격 정책 없음 NAME : {}", name);
            return new ApiException(ErrorCode.NOT_FOUND);
        });
    }
}
