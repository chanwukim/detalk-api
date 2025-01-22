package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.mock.FakePricingPlanCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PricingPlanServiceTest {

    private FakePricingPlanCache fakeCache;
    private PricingPlanService pricingPlanService;

    @BeforeEach
    public void setUp() {
        fakeCache = new FakePricingPlanCache();
        pricingPlanService = new PricingPlanService(fakeCache);
    }

    @DisplayName("성공[findByName] 캐시 조회 성공")
    @Test
    void findByName() {
        // given : 캐시에 미리 등록된 가격 정책
        PricingPlan dummyPlan = new PricingPlan(1L, "FREE");
        fakeCache.put(dummyPlan.getName(), dummyPlan);

        // when
        PricingPlan cachedPlan = pricingPlanService.findByName(dummyPlan.getName());

        // then
        assertThat(cachedPlan.getId()).isEqualTo(1L);
        assertThat(cachedPlan.getName()).isEqualTo(dummyPlan.getName());
    }

    @DisplayName("실패[findByName] 조회하려는 캐시가 메모리에 존재하지 않는다.")
    @Test
    public void testFindByName_NotFound_ThrowsException() {
        // given : 캐시에 데이터 없음

        // when & then : 존재하지 않는 이름으로 조회 시 예외 발생
        assertThrows(PricingPlanNotFoundException.class, () -> {
            pricingPlanService.findByName("NonExistingPlan");
        });
    }

}