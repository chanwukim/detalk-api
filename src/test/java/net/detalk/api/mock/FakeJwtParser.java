package net.detalk.api.mock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.HashMap;
import java.util.Map;
import net.detalk.api.support.security.jwt.JwtParserHolder;

public class FakeJwtParser implements JwtParserHolder {

    private final Map<String, Claims> tokenToClaims = new HashMap<>();
    private final Map<String, String> tokenToSubject = new HashMap<>();
    private RuntimeException exceptionToThrow = null;

    public FakeJwtParser() {}

    /**
     * 예외 상황을 시뮬레이션하기 위한 생성자
     * @param exceptionToThrow 토큰 파싱 시 발생시킬 예외
     */
    public FakeJwtParser(RuntimeException exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

    /**
     * 테스트용 토큰-클레임 매핑 설정
     * @param token 테스트할 토큰 문자열
     * @param claims 해당 토큰에 연결될 클레임 객체
     */
    public void setTokenClaims(String token, Claims claims) {
        tokenToClaims.put(token, claims);
    }

    /**
     * 테스트용 토큰-subject 매핑 설정
     * @param token 테스트할 토큰 문자열
     * @param subject 해당 토큰에 연결될 subject 값
     */
    public void setTokenSubject(String token, String subject) {
        tokenToSubject.put(token, subject);
    }

    /**
     * 토큰을 파싱하여 클레임 객체 반환
     * 미리 설정된 예외가 있으면 예외를 발생시키고,
     * 매핑된 클레임이 있으면 해당 클레임을 반환하며,
     * 그렇지 않으면 기본 클레임을 생성하여 반환
     *
     * @param token 파싱할 JWT 토큰
     * @return 파싱된 클레임 객체
     * @throws RuntimeException 미리 설정된 예외가 있는 경우
     */
    @Override
    public Claims parseSignedClaims(String token) {

        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }

        if (tokenToClaims.containsKey(token)) {
            return tokenToClaims.get(token);
        }

        return Jwts.claims()
            .subject(tokenToSubject.getOrDefault(token, "fake_subject"))
            .add("auth", "ROLE_USER")
            .build();
    }

    /**
     * 토큰에서 subject 값 추출
     * 미리 설정된 예외가 있으면 예외를 발생시키고,
     * 그렇지 않으면 매핑된 subject 또는 기본값 반환
     *
     * @param token 파싱할 JWT 토큰
     * @return 토큰의 subject 값
     * @throws RuntimeException 미리 설정된 예외가 있는 경우
     */
    @Override
    public String getSubject(String token) {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }

        return tokenToSubject.getOrDefault(token, "default-subject");
    }

}
