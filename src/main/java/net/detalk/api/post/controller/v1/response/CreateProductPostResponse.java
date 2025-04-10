package net.detalk.api.post.controller.v1.response;

/**
 * 상품 포스트 생성 응답 DTO
 *
 * @param id 생성된 상품 포스트 ID
 * @param shortLink 생성된 상품 포스트 단축 URL
 */
public record CreateProductPostResponse(
    Long id,
    String shortLink
) {
}
