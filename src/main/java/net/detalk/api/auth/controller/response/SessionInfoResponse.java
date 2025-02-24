package net.detalk.api.auth.controller.response;

import java.util.List;
import lombok.Builder;

/**
 세션 정보 응답 DTO
 @param id 회원 식별자
 @param userhandle 계정 고유 userHandle
 @param nickname 사용자 표시 이름
 @param description 자기소개 문구
 @param avatarUrl 프로필 이미지 URL
 @param roles 사용자 권한 목록
 */
@Builder
public record SessionInfoResponse(
    Long id,
    String userhandle,
    String nickname,
    String description,
    String avatarUrl,
    List<String> roles
) {

}
