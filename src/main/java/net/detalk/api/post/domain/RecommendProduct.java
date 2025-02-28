package net.detalk.api.post.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RecommendProduct {

    private Long id;
    // DB 저장되어 있는 추천 글 ID ("ex: 가격이 저렴해요")
    private Long recommendId;
    // 추천하려는 게시글 ID
    private Long productPostId;
    // 추천하려는 회원 ID
    private Long memberId;
    private Instant createdAt;
    // 사용자가 입력한 추천 내용 ("ex: 가격이 저렴하고, 사용하기 쉬워요")
    private String content;

    @Builder
    public RecommendProduct(Long id, Long recommendId, Long productPostId, Long memberId,
        Instant createdAt, String content) {
        this.id = id;
        this.recommendId = recommendId;
        this.productPostId = productPostId;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.content = content;
    }

}
