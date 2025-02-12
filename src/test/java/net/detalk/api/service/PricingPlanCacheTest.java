package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.repository.PricingPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingPlanCacheTest {

    @Mock
    private PricingPlanRepository pricingPlanRepository;
    private PricingPlanCache pricingPlanCache;

    @BeforeEach
    void setUp() {
        pricingPlanCache = new PricingPlanCache(pricingPlanRepository);
    }

    @DisplayName("[getPricingPlan] 캐시에 존재하지 않는 가격 정책 조회 시 예외 발생")
    @Test
    void getPricingPlan_WhenPlanNotFound_ShouldThrowException() {
        var planName = "NonExistentPlan";

        // when & then
        assertThatThrownBy(() -> pricingPlanCache.get(planName))
            .isInstanceOf(PricingPlanNotFoundException.class)
            .hasMessageContaining(String.format("가격 정책(NAME: %s)을 찾을 수 없습니다.", planName));
    }

    @DisplayName("[loadPricingPlans] DB에서 가격 정책을 로드하여 캐시에 저장되어야 한다")
    @Test
    void loadPricingPlans_ShouldLoadAndCachePricingPlans() {
        // given
        PricingPlan freePlan = PricingPlan.builder()
            .id(1L)
            .name("FREE")
            .build();

        List<PricingPlan> plans = List.of(freePlan);

        when(pricingPlanRepository.findAll()).thenReturn(plans);

        // when: 캐시 초기화
        pricingPlanCache.loadPricingPlans();

        // then 
        Map<String, PricingPlan> cache = pricingPlanCache.getAll();

        assertThat(cache)
            .isNotEmpty()
            .containsKey("FREE");

        assertThat(cache.get("FREE")).isEqualTo(freePlan);
    }

}