package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtParserHolder {

    Claims parseSignedClaims(String token);

    String getSubject(String token);

}
