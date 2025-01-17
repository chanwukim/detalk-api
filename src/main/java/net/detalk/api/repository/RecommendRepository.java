package net.detalk.api.repository;

import static net.detalk.jooq.tables.JRecommend.RECOMMEND;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.Recommend;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendRepository {

    private final DSLContext dsl;

    public Optional<Recommend> findByReason(String reason) {
        return dsl.selectFrom(RECOMMEND)
            .where(RECOMMEND.VALUE.eq(reason))
            .fetchOptionalInto(Recommend.class);
    }

    public Recommend save(String reason, Instant now) {
        return dsl.insertInto(RECOMMEND)
            .set(RECOMMEND.VALUE, reason)
            .set(RECOMMEND.CREATED_AT, now)
            .returning()
            .fetchOneInto(Recommend.class);
    }

}
