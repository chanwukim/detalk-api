package net.detalk.api.controller.v1.response;

import java.util.Date;

/**
 * 주의 : *어드민* API 응답용 현재 접속중인 세션 정보를 담는 DTO
 */
public record ActiveSessionResponse(
    Long userId,
    String userhandle,
    String sessionId,
    Date lastActiveTime
) {}
