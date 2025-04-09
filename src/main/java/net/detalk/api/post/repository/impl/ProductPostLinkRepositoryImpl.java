package net.detalk.api.post.repository.impl;

import static net.detalk.jooq.tables.JProductPostLink.PRODUCT_POST_LINK;

import lombok.RequiredArgsConstructor;
import net.detalk.api.post.domain.ProductPostLink;
import net.detalk.api.post.repository.ProductPostLinkRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductPostLinkRepositoryImpl implements ProductPostLinkRepository {

    private final DSLContext dsl;

    @Override
    public ProductPostLink save(Long postId, Long linkId, Long shortLinkId) {
        return dsl.insertInto(PRODUCT_POST_LINK)
            .set(PRODUCT_POST_LINK.POST_ID, postId)
            .set(PRODUCT_POST_LINK.LINK_ID, linkId)
            .set(PRODUCT_POST_LINK.SHORT_LINK_ID, shortLinkId)
            .returning()
            .fetchOneInto(ProductPostLink.class);
    }

}
