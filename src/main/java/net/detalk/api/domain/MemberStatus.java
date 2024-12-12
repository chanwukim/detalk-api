package net.detalk.api.domain;

import lombok.Getter;

@Getter
public enum MemberStatus {
    PENDING("pending"),
    ACTIVE("active");

    private final String status;

    MemberStatus(String status) {
        this.status = status;
    }
}
