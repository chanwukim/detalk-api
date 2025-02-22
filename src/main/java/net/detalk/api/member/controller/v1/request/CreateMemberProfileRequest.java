package net.detalk.api.member.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 프로필 등록 요청 DTO
 *
 * @param userhandle 계정 고유 식별자 (64자 이내)
 * @param nickname   사용자 표시 이름 (2~20자)
 */
public record CreateMemberProfileRequest(
    @NotBlank(message = "Userhandle is required")
    @Size(max = 64, message = "Userhandle must be less than or equal to 64 characters")
    String userhandle,

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 20, message = "Nickname must be a minimum of 2 characters and a maximum of 20 characters")
    String nickname
) {
}
