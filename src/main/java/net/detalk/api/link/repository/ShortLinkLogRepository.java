package net.detalk.api.link.repository;

import net.detalk.api.link.domain.ShortLinkLog;

public interface ShortLinkLogRepository {
    ShortLinkLog save(ShortLinkLog logEntry);
}
