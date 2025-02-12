package net.detalk.api.domain;

public record AuthToken (
    String accessToken,
    String refreshToken
) {
}
