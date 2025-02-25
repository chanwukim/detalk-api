package net.detalk.api.post.controller.v1.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/**
 * 상품 게시글 생성 요청 DTO
 *
 * @param name          상품 이름 (필수, 255자 이내)
 * @param url           상품 URL (필수, 유효한 URL 형식)
 * @param description   상품 설명 (필수, 1000자 이내)
 * @param imageIds      이미지 ID 목록 (필수, null 불가)
 * @param isMaker       메이커 여부
 * @param tags          태그 목록 (최대 10개, 각 태그 32자 이내)
 * @param pricingPlan   가격 정책 (필수, 255자 이내)
 * @param idempotentKey 멱등키 (필수)
 */
@Builder
public record CreateProductPostRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than or equal to 255 characters")
    String name,

    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL must be less than or equal to 255 characters")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].\\S*$", message = "URL must be valid")
    String url,

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be less than or equal to 1000 characters")
    String description,

    @NotNull(message = "Image IDs cannot be null")
    List<@NotNull(message = "Image ID cannot be null") String> imageIds,

    boolean isMaker,

    @NotNull(message = "Tags cannot be null")
    @Size(max = 10, message = "Cannot have more than 10 tags")
    List<@NotBlank(message = "Tag cannot be blank") @Size(max = 32, message = "Tag must be less than or equal to 32 characters") String> tags,

    @NotBlank(message = "Pricing Plan is required")
    @Size(max = 255, message = "Pricing Plan must be less than or equal to 255 characters")
    String pricingPlan,

    @NotBlank(message = "idempotentKey is required")
    String idempotentKey

) {}

