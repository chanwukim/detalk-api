package net.detalk.api.link.repository;

import static net.detalk.jooq.tables.JShortLinks.SHORT_LINKS;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.link.domain.ShortLink;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ShortLinkRepositoryImpl implements ShortLinkRepository{

    private final DSLContext dsl;

    @Override
    public ShortLink save(String shortCode, String originalUrl, Long creatorId, Instant createdAt) {
        return dsl.insertInto(SHORT_LINKS)
            .set(SHORT_LINKS.SHORT_CODE, shortCode)
            .set(SHORT_LINKS.ORIGINAL_URL, originalUrl)
            .set(SHORT_LINKS.CREATOR_ID, creatorId)
            .set(SHORT_LINKS.CREATED_AT, createdAt)
            .returning()
            .fetchOneInto(ShortLink.class);
    }

    @Override
    public Optional<String> findOriginalUrlByShortCode(String shortCode) {
        return dsl.select(SHORT_LINKS.ORIGINAL_URL)
            .from(SHORT_LINKS)
            .where(SHORT_LINKS.SHORT_CODE.eq(shortCode))
            .fetchOptional(SHORT_LINKS.ORIGINAL_URL);
    }
}
