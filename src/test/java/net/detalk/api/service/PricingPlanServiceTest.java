package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.detalk.api.plan.domain.PricingPlan;
import net.detalk.api.plan.domain.exception.PricingPlanNotFoundException;
import net.detalk.api.mock.FakePricingPlanCache;
import net.detalk.api.plan.repository.PricingPlanRepository;
import net.detalk.api.plan.service.PricingPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingPlanServiceTest {

    private FakePricingPlanCache fakeCache;
    private PricingPlanService pricingPlanService;

    @Mock
    private PricingPlanRepository pricingPlanRepository;

    @BeforeEach
    void setUp() {
        fakeCache = new FakePricingPlanCache();
        pricingPlanService = new PricingPlanService(pricingPlanRepository);
    }

    @DisplayName("성공[findByName] 캐시 조회 성공")
    @Test
    void findByName() {
        // given : 캐시에 미리 등록된 가격 정책
        PricingPlan dummyPlan = new PricingPlan(1L, "FREE");
        fakeCache.put(dummyPlan.getName(), dummyPlan);

        when(pricingPlanRepository.findByName(dummyPlan.getName())).thenReturn(
            Optional.of(dummyPlan));

        // when
        PricingPlan cachedPlan = pricingPlanService.findByName(dummyPlan.getName());

        // then
        assertThat(cachedPlan.getId()).isEqualTo(1L);
        assertThat(cachedPlan.getName()).isEqualTo(dummyPlan.getName());
    }

    @DisplayName("실패[findByName] 조회하려는 캐시가 메모리에 존재하지 않는다.")
    @Test
    void testFindByName_NotFound_ThrowsException() {
        // given : 캐시에 데이터 없음

        // when & then : 존재하지 않는 이름으로 조회 시 예외 발생
        assertThrows(PricingPlanNotFoundException.class, () -> {
            pricingPlanService.findByName("NonExistingPlan");
        });
    }

}