package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;
import net.detalk.api.support.TimeHolder;

import java.time.Instant;

@Getter
public class Member {
    private Long id;
    private LoginType loginType;
    private MemberStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    @Builder
    public Member(Long id, LoginType loginType, MemberStatus status, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.loginType = loginType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean isPendingExternalMember() {
        return loginType == LoginType.EXTERNAL && status == MemberStatus.PENDING;
    }

    public boolean isNewMember() {
        return status == MemberStatus.PENDING;
    }

    public void active(TimeHolder timeHolder) {
        status = MemberStatus.ACTIVE;
        this.updatedAt = timeHolder.now();
    }

}
