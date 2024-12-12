package net.detalk.api.support.security;

import net.detalk.api.support.AppProperties;
import net.detalk.api.support.error.ExpiredTokenException;
import net.detalk.api.support.error.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenProviderTest {
    private TokenProvider tokenProvider;
    private static final String TOKEN_SECRET = "SECRETSECRETSECRETSECRETSECRETSECRETSECRET";
    private static final long ACCESS_TOKEN_EXPIRES = 3600L;      // 1시간
    private static final long REFRESH_TOKEN_EXPIRES = 1209600L;  // 2주

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setTokenSecret(TOKEN_SECRET);
        appProperties.setAccessTokenExpiresInSeconds(ACCESS_TOKEN_EXPIRES);
        appProperties.setRefreshTokenExpiresInSeconds(REFRESH_TOKEN_EXPIRES);

        tokenProvider = new TokenProvider(appProperties);
    }

    @Test
    @DisplayName("액세스 토큰 생성 및 파싱 테스트")
    void createAndParseAccessToken() {
        // given
        Long memberId = 1L;

        // when
        AccessToken accessToken = tokenProvider.createAccessToken(memberId);
        AccessToken parsedToken = tokenProvider.parseAccessToken(accessToken.getValue());

        // then
        assertThat(parsedToken.getMemberId()).isEqualTo(memberId);
        assertThat(parsedToken.getExpiresAt().getTime() - parsedToken.getIssuedAt().getTime())
            .isEqualTo(ACCESS_TOKEN_EXPIRES * 1000L);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void createRefreshToken() {
        // when
        RefreshToken refreshToken = tokenProvider.createRefreshToken();

        // then
        assertThat(refreshToken).isNotNull();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 파싱시 예외 발생")
    void parseInvalidToken() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThatThrownBy(() -> tokenProvider.parseAccessToken(invalidToken))
            .isInstanceOf(TokenException.class)
            .hasMessage("Invalid token");
    }

    @Test
    @DisplayName("만료된 토큰 파싱시 예외 발생")
    void parseExpiredToken() {
        // given
        AppProperties expiredProperties = new AppProperties();
        expiredProperties.setTokenSecret(TOKEN_SECRET);
        expiredProperties.setAccessTokenExpiresInSeconds(-1L);
        TokenProvider expiredTokenProvider = new TokenProvider(expiredProperties);

        AccessToken expiredToken = expiredTokenProvider.createAccessToken(1L);

        // when & then
        assertThatThrownBy(() -> tokenProvider.parseAccessToken(expiredToken.getValue()))
            .isInstanceOf(ExpiredTokenException.class);
    }

}