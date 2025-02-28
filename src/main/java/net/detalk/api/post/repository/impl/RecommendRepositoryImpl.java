package net.detalk.api.post.repository.impl;

import static net.detalk.jooq.tables.JRecommend.RECOMMEND;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.post.domain.Recommend;
import net.detalk.api.post.repository.RecommendRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendRepositoryImpl implements RecommendRepository {

    private final DSLContext dsl;

    @Override
    public Optional<Recommend> findByReason(String reason) {
        return dsl.selectFrom(RECOMMEND)
            .where(RECOMMEND.VALUE.eq(reason))
            .fetchOptionalInto(Recommend.class);
    }

    @Override
    public Recommend save(String reason, Instant now) {
        return dsl.insertInto(RECOMMEND)
            .set(RECOMMEND.VALUE, reason)
            .set(RECOMMEND.CREATED_AT, now)
            .returning()
            .fetchOneInto(Recommend.class);
    }

}
