package net.detalk.api.post.controller.v1.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

/**
 * 상품 포스트 조회 응답 DTO
 *
 * @param id            포스트 ID
 * @param nickname      작성자 닉네임
 * @param userHandle    작성자 핸들
 * @param createdAt     생성 시간
 * @param isMaker       메이커 여부
 * @param avatarUrl     프로필 이미지 URL
 * @param title         상품 제목
 * @param description   상품 설명
 * @param pricingPlan   가격 정책
 * @param recommendCount 추천 수
 * @param tags          태그 목록
 * @param media         미디어 정보 목록
 * @param urls          관련 URL 목록
 * @param shortCode     단축 URL
 */
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
    List<String> urls,
    String shortCode
) {
    public record Media(
        String url,
        Integer sequence
    ) {}
}



