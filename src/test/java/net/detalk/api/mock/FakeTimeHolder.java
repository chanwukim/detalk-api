package net.detalk.api.mock;

import java.time.Instant;
import net.detalk.api.support.TimeHolder;


public class FakeTimeHolder implements TimeHolder {

    private final Instant now;

    /**
     * @param now Instant.parse("2025-01-01T12:00:00Z")
     */
    public FakeTimeHolder(Instant now) {
        this.now = now;
    }

    @Override
    public Instant now() {
        return now;
    }

}
