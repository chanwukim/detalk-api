package net.detalk.api.repository;

import static net.detalk.jooq.tables.JVisitorLog.VISITOR_LOG;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.VisitorLog;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class VisitorLogRepository {

    private final DSLContext dsl;

    public VisitorLog save(VisitorLog visitorLog) {
        return dsl.insertInto(VISITOR_LOG)
            .set(VISITOR_LOG.SESSION_ID, visitorLog.getSessionId())
            .set(VISITOR_LOG.CONTINENT_CODE, visitorLog.getContinentCode())
            .set(VISITOR_LOG.COUNTRY_ISO, visitorLog.getCountryIso())
            .set(VISITOR_LOG.COUNTRY_NAME, visitorLog.getCountryName())
            .set(VISITOR_LOG.VISITED_AT, visitorLog.getVisitedAt())
            .set(VISITOR_LOG.USER_AGENT, visitorLog.getUserAgent())
            .set(VISITOR_LOG.REFERER, visitorLog.getReferer())
            .returning()
            .fetchOneInto(VisitorLog.class);
    }

    public Optional<VisitorLog> findById(Long id) {
        return dsl.selectFrom(VISITOR_LOG)
            .where(VISITOR_LOG.ID.eq(id))
            .fetchOptionalInto(VisitorLog.class);
    }

    public List<VisitorLog> findAll() {
        return dsl.selectFrom(VISITOR_LOG)
            .fetchInto(VisitorLog.class);
    }
}
