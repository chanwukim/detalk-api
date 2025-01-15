package net.detalk.api.repository;

import static net.detalk.jooq.tables.JPricingPlan.PRICING_PLAN;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.PricingPlan;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PricingPlanRepository {

    private final DSLContext dsl;

    public Optional<PricingPlan> findByName(String name) {
        return dsl.selectFrom(PRICING_PLAN)
            .where(PRICING_PLAN.NAME.eq(name))
            .fetchOptionalInto(PricingPlan.class);
    }

    public PricingPlan save(PricingPlan plan) {
        return dsl.insertInto(PRICING_PLAN)
            .set(PRICING_PLAN.NAME, plan.getName())
            .returning()
            .fetchOneInto(PricingPlan.class);
    }

    public List<String> findAllNames() {
        return dsl.select(PRICING_PLAN.NAME)
            .from(PRICING_PLAN)
            .fetch(PRICING_PLAN.NAME);
    }

    public List<PricingPlan> findAll() {
        return dsl.select(PRICING_PLAN)
            .from(PRICING_PLAN)
            .fetchInto(PricingPlan.class);
    }

    public void saveAll(List<PricingPlan> planList) {
        BatchBindStep batch = dsl.batch(
            dsl.insertInto(PRICING_PLAN)
                .set(PRICING_PLAN.NAME, (String) null)
        );

        for (PricingPlan plan : planList) {
            batch.bind(plan.getName());
        }

        batch.execute();

    }
}
