package net.detalk.api.support.security.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "security.auth.type", havingValue = "jwt")
@Configuration
public class JwtConstants {

    @Value("${jwt.secret}")
    private String secretKey;

    @Getter
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Getter
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Getter
    @Value("${jwt.refresh-path}")
    private String refreshPath;

    @Bean
    public Key jwtSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public JwtParser jwtParser() {
        return Jwts.parser()
            .verifyWith((SecretKey) jwtSecretKey())
            .build();
    }
}