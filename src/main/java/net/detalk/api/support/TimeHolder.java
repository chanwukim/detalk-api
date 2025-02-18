package net.detalk.api.support;

import java.time.Instant;
import java.time.LocalDateTime;

public interface TimeHolder {
    Instant now();

    LocalDateTime dateTime();

}
