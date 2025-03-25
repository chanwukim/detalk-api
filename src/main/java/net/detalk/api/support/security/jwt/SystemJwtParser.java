package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SystemJwtParser implements JwtParserHolder {

    private final JwtParser jwtParser;

    @Override
    public Claims parseSignedClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    @Override
    public String getSubject(String token) {
        return parseSignedClaims(token).getSubject();
    }

}
