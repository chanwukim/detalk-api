package net.detalk.api.auth.controller.v2.response;

public record JwtTokenResponse(
    String accessToken,
    String refreshToken
) {}
