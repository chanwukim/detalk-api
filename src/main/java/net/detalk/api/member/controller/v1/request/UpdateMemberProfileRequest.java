package net.detalk.api.member.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 회원 프로필 수정 요청 DTO
 *
 * @param userhandle 계정 식별자 (필수, 64자 이내)
 * @param avatarId   아바타 이미지 ID
 * @param nickname   사용자 표시명 (필수, 2~20자)
 * @param description 자기소개 (1000자 이내)
 */
@Builder
public record UpdateMemberProfileRequest(

    @NotBlank(message = "Userhandle is required")
    @Size(max = 64, message = "Userhandle must be less than or equal to 64 characters")
    String userhandle,

    String avatarId,

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 20, message = "Nickname must be a minimum of 2 characters and a maximum of 20 characters")
    String nickname,

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description
) {}
