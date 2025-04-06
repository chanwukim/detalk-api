package net.detalk.api.mock;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import net.detalk.api.support.security.jwt.JwtConstants;

public class FakeJwtConstants implements JwtConstants {

    private final Key secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private final String refreshPath;
    private final String accessPath;

    public FakeJwtConstants(String secretKey, long accessTokenValidity, long refreshTokenValidity,
        String refreshPath, String accessPath) {
        byte[] keyBytes = secretKey.getBytes();
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.refreshPath = refreshPath;
        this.accessPath= accessPath;
    }

    @Override
    public Key jwtSecretKey() {
        return secretKey;
    }

    @Override
    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    @Override
    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    @Override
    public String getRefreshPath() {
        return refreshPath;
    }

    @Override
    public String getAccessPath() {
        return accessPath;
    }
}
