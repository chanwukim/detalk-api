package net.detalk.api.support.security;

import lombok.Getter;

@Getter
public enum SecurityRole {
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String name;

    SecurityRole(String name) {
        this.name = name;
    }
}
