package net.detalk.api.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Recommend {

    private Long id;
    private String value;
    private Instant createdAt;

    @Builder
    public Recommend(Long id, String value, Instant createdAt) {
        this.id = id;
        this.value = value;
        this.createdAt = createdAt;
    }
}
