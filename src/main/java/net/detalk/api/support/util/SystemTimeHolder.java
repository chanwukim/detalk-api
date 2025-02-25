package net.detalk.api.support.util;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemTimeHolder implements TimeHolder {
    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public LocalDateTime dateTime() {
        return LocalDateTime.now();
    }
}
