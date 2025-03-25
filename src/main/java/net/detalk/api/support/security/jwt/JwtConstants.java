package net.detalk.api.support.security.jwt;

import java.security.Key;

public interface JwtConstants {

    Key jwtSecretKey();

    long getAccessTokenValidity();

    long getRefreshTokenValidity();

    String getRefreshPath();

    String getAccessPath();

}
