package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
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
