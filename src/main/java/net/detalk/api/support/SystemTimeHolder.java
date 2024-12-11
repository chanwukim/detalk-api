package net.detalk.api.support;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SystemTimeHolder implements TimeHolder {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
