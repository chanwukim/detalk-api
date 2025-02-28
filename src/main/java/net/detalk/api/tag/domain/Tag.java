package net.detalk.api.tag.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Tag {

    private Long id;
    private String name;

    @Builder
    public Tag(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
