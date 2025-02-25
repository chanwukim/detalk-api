package net.detalk.api.mock;

import java.time.Instant;
import java.time.LocalDateTime;
import net.detalk.api.support.util.TimeHolder;


public class FakeTimeHolder implements TimeHolder {

    private final Instant now;
    private final LocalDateTime localDateTime;

    /**
     * @param now Instant.parse("2025-01-01T12:00:00Z")
     */
    public FakeTimeHolder(Instant now, LocalDateTime localDateTime) {
        this.now = now;
        this.localDateTime = localDateTime;
    }

    @Override
    public Instant now() {
        return now;
    }

    @Override
    public LocalDateTime dateTime() {
        return localDateTime;
    }

}
