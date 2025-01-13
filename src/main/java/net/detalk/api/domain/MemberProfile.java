package net.detalk.api.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import net.detalk.api.controller.v1.request.UpdateProfileRequest;

@Getter
public class MemberProfile {
    private Long id;
    private Long memberId;
    private UUID avatarId;
    private String userhandle;
    private String nickname;
    private String description;
    private Instant updatedAt;

    @Builder
    public MemberProfile(Long id, Long memberId, UUID avatarId, String userhandle, String nickname, String description,
                         Instant updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.avatarId = avatarId;
        this.userhandle = userhandle;
        this.nickname = nickname;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public MemberProfile update(UpdateProfileRequest updateProfile, UUID newAvatarId,
        Instant now) {
        return MemberProfile.builder()
            .id(id)
            .memberId(memberId)
            .avatarId(newAvatarId)
            .userhandle(updateProfile.userandle())
            .nickname(updateProfile.nickname())
            .description(updateProfile.description())
            .updatedAt(now)
            .build();
    }

    public boolean hasSameUserHandle(String userhandle) {
        return Objects.equals(this.userhandle, userhandle);
    }
}
