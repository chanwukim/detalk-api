package net.detalk.api.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductCreate {

    private String name;
    private Long writerId;
    private String url;
    private String description;
    private List<Long> imageIds;
    private boolean isMaker;
    private List<String> tags;
    private String pricingPlan;

    @Builder
    public ProductCreate(
        String name, Long writerId, String url,
        String description, List<Long> imageIds, boolean isMaker,
        List<String> tags, String pricingPlan) {
        this.name = name;
        this.writerId = writerId;
        this.url = url;
        this.description = description;
        this.imageIds = imageIds;
        this.isMaker = isMaker;
        this.tags = tags;
        this.pricingPlan = pricingPlan;
    }

}
