package net.detalk.api.domain;

import lombok.Getter;

@Getter
public enum MemberRole {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String name;

    MemberRole(String name) {
        this.name = name;
    }
}
