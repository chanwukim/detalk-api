package net.detalk.api.support.util;

import org.jooq.Converter;

import java.time.Instant;

public class InstantConverter implements Converter<Long, Instant> {
    @Override
    public Instant from(Long databaseObject) {
        return databaseObject == null ? null : Instant.ofEpochMilli(databaseObject);
    }

    @Override
    public Long to(Instant userObject) {
        return userObject == null ? null : userObject.toEpochMilli();
    }

    @Override
    public Class<Long> fromType() {
        return Long.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
