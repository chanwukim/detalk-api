package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PricingPlan {

    private Long id;
    private String name;

    @Builder
    public PricingPlan(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return
            "{name='" + name + '\'' +
            '}';
    }
}
