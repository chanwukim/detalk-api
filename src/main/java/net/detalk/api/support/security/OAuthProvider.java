package net.detalk.api.support.security;

import lombok.Getter;

@Getter
public enum OAuthProvider {
    GOOGLE("google");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }
}
