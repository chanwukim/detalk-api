package net.detalk.api.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.repository.PricingPlanRepository;
import org.springframework.stereotype.Component;

/**
 * 캐시된 가격 정책
 * TODO : 추가, 삭제 기능 및 캐시 만료 정책, 캐시 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PricingPlanCache implements DetalkCache<String,PricingPlan>{

    private final ConcurrentMap<String, PricingPlan> cache = new ConcurrentHashMap<>();

    private final PricingPlanRepository pricingPlanRepository;

    /**
     * 톰캣 실행 시, 실행된다.
     * 초기 가격 정책 캐시 설정
     */
    protected void loadPricingPlans() {
        log.info("캐시용 가격 정책 목록 DB에서 조회중...");
        List<PricingPlan> plans = pricingPlanRepository.findAll();
        plans.forEach(plan -> cache.put(plan.getName(), plan));
        log.info("총 {}건의 가격 정책이 캐시되었습니다. {}", cache.size(), plans);
    }

    /**
     * 캐싱되어 있는 가격 정책 조회
     * @param name 가격 정책 이름
     * @return 가격 정책
     * @throws PricingPlanNotFoundException 가격 정책을 찾을 수 없는 경우
     */
    @Override
    public PricingPlan get(String name) {
        PricingPlan plan = cache.get(name);
        if (plan == null) {
            log.error("[getPricingPlan] 가격 정책을 찾을 수 없습니다: {}", name);
            throw new PricingPlanNotFoundException(name);
        }
        return plan;
    }

    @Override
    public void put(String key, PricingPlan value) {
        cache.put(key, value);
    }

    /**
     * 캐시된 전체 가격 정책 조회
     * @return 외부에선 변경 불가능한 캐시 가격 정책
     */
    @Override
    public Map<String, PricingPlan> getAll() {
        return Collections.unmodifiableMap(cache);
    }

}
