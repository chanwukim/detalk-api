package net.detalk.api.auth.domain;

public record AuthToken (
    String accessToken,
    String refreshToken
) {
}
