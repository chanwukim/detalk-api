package net.detalk.api.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Product {

    private Long id;
    private String name;
    private Instant cratedAt;

    @Builder
    public Product(Long id, String name, Instant cratedAt) {
        this.id = id;
        this.name = name;
        this.cratedAt = cratedAt;
    }
}
