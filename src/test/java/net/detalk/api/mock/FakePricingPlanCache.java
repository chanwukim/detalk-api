package net.detalk.api.mock;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.detalk.api.plan.domain.PricingPlan;
import net.detalk.api.plan.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.support.cache.DetalkCache;

/**
 * @deprecated 이 클래스는 더 이상 사용되지 않으며 향후 제거될 예정
 * 카페인 캐시를 이용합니다
 */
@Deprecated(since = "0.2", forRemoval = true)
public class FakePricingPlanCache implements DetalkCache<String, PricingPlan> {

    private final Map<String, PricingPlan> cache = new ConcurrentHashMap<>();


    @Override
    public PricingPlan get(String key) {
        PricingPlan plan = cache.get(key);
        if (plan == null) {
            throw new PricingPlanNotFoundException(key);
        }
        return plan;
    }

    @Override
    public void put(String key, PricingPlan value) {
        cache.put(key, value);
    }

    @Override
    public Map<String, PricingPlan> getAll() {
        return Collections.unmodifiableMap(cache);
    }
}
