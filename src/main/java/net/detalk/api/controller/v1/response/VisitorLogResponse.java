package net.detalk.api.controller.v1.response;

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
