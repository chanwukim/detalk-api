package net.detalk.api.plan.repository;

import java.util.List;
import java.util.Optional;
import net.detalk.api.plan.domain.PricingPlan;

public interface PricingPlanRepository {

    Optional<PricingPlan> findByName(String name);

    PricingPlan save(PricingPlan plan);

    List<String> findAllNames();

    List<PricingPlan> findAll();

    void saveAll(List<PricingPlan> planList);

}
