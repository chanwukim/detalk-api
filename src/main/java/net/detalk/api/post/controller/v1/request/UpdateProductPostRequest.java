package net.detalk.api.post.controller.v1.request;

import java.util.List;
import lombok.Builder;

/**
 * 상품 포스트 수정 요청 DTO
 *
 * @param name        상품 이름
 * @param pricingPlan 가격 정책
 * @param description 상품 설명
 * @param tags        태그 목록
 * @param url         상품 URL
 * @param imageIds    이미지 ID 목록
 * @param isMaker     메이커 여부
 */
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
