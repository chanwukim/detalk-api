package net.detalk.api.controller.v1.request;

import java.util.List;
import lombok.Builder;

@Builder
public record UpdateProductPostRequest(
    String name,
    String pricingPlan,
    String description,
    List<String> tags,
    String url,
    List<String> imageIds,
    boolean isMaker
) {
}
