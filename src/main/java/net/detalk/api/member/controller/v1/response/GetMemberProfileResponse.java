package net.detalk.api.member.controller.v1.response;

import lombok.Builder;

/**
 * 회원 프로필 정보 응답 DTO
 *
 * @param id         회원 식별자
 * @param userhandle 계정 고유 ID
 * @param nickname   사용자 표시 이름
 * @param description 자기소개 문구
 * @param avatarUrl  프로필 이미지 URL
 */
@Builder
public record GetMemberProfileResponse(
    Long id,
    String userhandle,
    String nickname,
    String description,
    String avatarUrl
) {}
