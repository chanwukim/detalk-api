package net.detalk.api.link.repository;

import static net.detalk.jooq.tables.JShortLinksLogs.SHORT_LINKS_LOGS;

import lombok.RequiredArgsConstructor;
import net.detalk.api.link.domain.ShortLinkLog;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ShortLinkLogRepositoryImpl implements ShortLinkLogRepository {

    private final DSLContext dsl;

    @Override
    public ShortLinkLog save(ShortLinkLog logEntry) {
        return dsl.insertInto(SHORT_LINKS_LOGS)
            .set(SHORT_LINKS_LOGS.LINK_ID, logEntry.getLinkId())
            .set(SHORT_LINKS_LOGS.CLICKED_AT, logEntry.getClickedAt())
            .set(SHORT_LINKS_LOGS.IP_ADDRESS, logEntry.getIpAddress())
            .set(SHORT_LINKS_LOGS.USER_AGENT, logEntry.getUserAgent())
            .set(SHORT_LINKS_LOGS.REFERRER, logEntry.getReferrer())
            .set(SHORT_LINKS_LOGS.COUNTRY, logEntry.getCountry())
            .set(SHORT_LINKS_LOGS.CITY, logEntry.getCity())
            .set(SHORT_LINKS_LOGS.DEVICE_TYPE, logEntry.getDeviceType())
            .set(SHORT_LINKS_LOGS.OS, logEntry.getOs())
            .set(SHORT_LINKS_LOGS.BROWSER, logEntry.getBrowser())
            .returning()
            .fetchOneInto(ShortLinkLog.class);
    }
}
