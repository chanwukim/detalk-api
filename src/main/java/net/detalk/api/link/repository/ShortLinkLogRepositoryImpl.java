package net.detalk.api.link.repository;

import static net.detalk.jooq.tables.JShortLinksLogs.SHORT_LINKS_LOGS;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.link.domain.CountryStatPoint;
import net.detalk.api.link.domain.ShortLinkLog;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
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

    @Override
    public List<CountryStatPoint> getClickCountsByLinkId(long linkId) {

        // todo : 현재는 count 함수를 이용하지만, 성능을 위해 따로 count 컬럼을 추가해야할수도?
        var clickCount = DSL.count().as("click_count");
        var countryName = DSL.coalesce(SHORT_LINKS_LOGS.COUNTRY, "Unknown").as("country_name");

        var result = dsl.select(
                countryName,
                clickCount
            )
            .from(SHORT_LINKS_LOGS)
            .where(SHORT_LINKS_LOGS.LINK_ID.eq(linkId))
            .groupBy(countryName)
            .fetch();

        return result.map(record -> new CountryStatPoint(
            record.value1(),
            record.value2().longValue()
        ));
    }

}
