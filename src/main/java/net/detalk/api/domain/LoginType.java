package net.detalk.api.domain;

import lombok.Getter;

@Getter
public enum LoginType {
    EMAIL("email"),
    EXTERNAL("external");

    private final String value;

    LoginType(String value) {
        this.value = value;
    }
}
