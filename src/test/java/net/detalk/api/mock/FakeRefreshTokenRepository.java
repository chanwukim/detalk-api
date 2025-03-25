package net.detalk.api.mock;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.detalk.api.auth.domain.RefreshToken;
import net.detalk.api.auth.repository.RefreshTokenRepository;

public class FakeRefreshTokenRepository implements RefreshTokenRepository {

    private final Map<Long, RefreshToken> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        // ID가 없는 경우, ID 생성
        if (refreshToken.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            refreshToken = RefreshToken.builder()
                .id(newId)
                .memberId(refreshToken.getMemberId())
                .token(refreshToken.getToken())
                .createdAt(refreshToken.getCreatedAt())
                .expiresAt(refreshToken.getExpiresAt())
                .revokedAt(refreshToken.getRevokedAt())
                .build();
        }
        store.put(refreshToken.getId(), refreshToken);
        return refreshToken;
    }

    @Override
    public void revokeByMemberId(Long memberId, Instant now) {
// memberId에 해당하는 RefreshToken을 찾아서 revokedAt을 설정
        store.values().stream()
            .filter(
                token -> token.getMemberId().equals(memberId)
                && token.getRevokedAt() == null)
            .findFirst() // 여러개일경우, jooq 쿼리처럼 가장 첫번째것만 revoke
            .ifPresent(refreshToken -> {
                RefreshToken revokedToken = RefreshToken.builder()
                    .id(refreshToken.getId()) // 기존 ID 유지
                    .memberId(refreshToken.getMemberId())
                    .token(refreshToken.getToken())
                    .createdAt(refreshToken.getCreatedAt())
                    .expiresAt(refreshToken.getExpiresAt())
                    .revokedAt(now) // 현재 시간으로 revokedAt 설정
                    .build();
                store.put(refreshToken.getId(), revokedToken); // 업데이트된 토큰 저장
            });
    }

    @Override
    public boolean isActiveToken(String token, boolean revoked) {

        RefreshToken refreshToken =  store.values().stream()
            .filter(t -> t.getToken().equals(token))
            .findFirst()
            .orElse(null); //토큰이 없을경우 null

        if(refreshToken == null){
            return false; // not found
        }

        boolean isRevoked = refreshToken.getRevokedAt() != null;  //취소 여부
        boolean isExpired = refreshToken.getExpiresAt().isBefore(Instant.now()); // 만료 여부

        if(revoked){ //파라미터 revoked == true. 즉, 비활성화된 토큰을 찾는경우
            return isRevoked; //비활성화 되었으면 true, 아니면 false
        }
        else{ //활성화된 토큰을 찾는경우
            return !isRevoked && !isExpired; // 취소되지 않았고, 만료되지 않았으면 true
        }
    }
}
