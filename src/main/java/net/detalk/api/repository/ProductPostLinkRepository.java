package net.detalk.api.repository;

import static net.detalk.jooq.tables.JProductPostLink.PRODUCT_POST_LINK;

import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.ProductPostLink;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostLinkRepository {

    private final DSLContext dsl;

    public ProductPostLink save(Long postId, Long linkId) {
        return dsl.insertInto(PRODUCT_POST_LINK)
            .set(PRODUCT_POST_LINK.POST_ID, postId)
            .set(PRODUCT_POST_LINK.LINK_ID, linkId)
            .returning()
            .fetchOneInto(ProductPostLink.class);
    }

}
