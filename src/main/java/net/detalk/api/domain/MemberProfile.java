package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class MemberProfile {
    private Long id;
    private Long memberId;
    private Long avatarId;
    private String userhandle;
    private String nickname;
    private String description;
    private Instant updatedAt;

    @Builder
    public MemberProfile(Long id, Long memberId, Long avatarId, String userhandle, String nickname, String description,
            Instant updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.avatarId = avatarId;
        this.userhandle = userhandle;
        this.nickname = nickname;
        this.description = description;
        this.updatedAt = updatedAt;
    }
}
