package net.detalk.api.controller.v1.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record GetProductPostResponse(
    Long id,
    String nickname,
    String userHandle,
    Instant createdAt,
    Boolean isMaker,
    String avatarUrl,
    String title,
    String description,
    String pricingPlan,
    Integer recommendCount,
    List<String> tags,
    List<Media> media,
    List<String> urls
) {
    public record Media(
        String url,
        Integer sequence
    ) {}
}



