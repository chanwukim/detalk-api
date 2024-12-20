package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateRecommend {

    private String reason;
    private Long memberId;

    @Builder
    public CreateRecommend(String reason, Long memberId) {
        this.reason = reason;
        this.memberId = memberId;
    }
}
