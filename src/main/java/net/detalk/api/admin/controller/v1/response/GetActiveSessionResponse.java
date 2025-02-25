package net.detalk.api.admin.controller.v1.response;

import java.util.Date;

/**
 * 현재 접속 중인 세션 정보 응답 DTO (주의 : 어드민 API 전용)
 * @param userId 회원 식별자
 * @param userhandle 계정 고유 userHandle
 * @param sessionId 세션 식별자
 * @param lastActiveTime 마지막 활동 시간
 */
public record GetActiveSessionResponse(
    Long userId,
    String userhandle,
    String sessionId,
    Date lastActiveTime
) {}
