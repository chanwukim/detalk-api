package net.detalk.api.support.security;

import lombok.Getter;

@Getter
public enum SecurityRole {

    /**
     * SecurityRole.MEMBER.name() : "MEMBER" 반환
     * SecurityRole.MEMBER.getName() : "ROLE_MEMBER" 반환
     */
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String name;

    SecurityRole(String name) {
        this.name = name;
    }

    public String getDescription() {
        return switch (this) {
            case MEMBER -> "일반 회원";
            case ADMIN -> "관리자";
        };
    }
}
