package net.detalk.api.domain;

import lombok.Getter;

@Getter
public class Role {
    private String code;
    private String description;

    public Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
