package net.detalk.api.admin.controller.response;

import java.time.LocalDateTime;

public record VisitorLogResponse(
    String sessionId,
    String continentCode,
    String countryIso,
    String countryName,
    LocalDateTime visitedAt,
    String userAgent,
    String referer
) {

}
