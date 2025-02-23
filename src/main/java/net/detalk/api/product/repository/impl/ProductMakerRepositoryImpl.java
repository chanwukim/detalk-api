package net.detalk.api.product.repository.impl;

import static net.detalk.jooq.tables.JProductMaker.*;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.detalk.api.product.domain.ProductMaker;
import net.detalk.api.product.repository.ProductMakerRepository;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductMakerRepositoryImpl implements ProductMakerRepository {

    private final DSLContext dsl;

    @Override
    public ProductMaker save(ProductMaker maker) {
        return dsl.insertInto(PRODUCT_MAKER)
            .set(PRODUCT_MAKER.PRODUCT_ID, maker.getProductId())
            .set(PRODUCT_MAKER.MEMBER_ID, maker.getMemberId())
            .set(PRODUCT_MAKER.CREATED_AT, maker.getCreatedAt())
            .returning()
            .fetchOneInto(ProductMaker.class);
    }


    @Override
    public Optional<ProductMaker> findByProductIdAndMemberId(Long productId, Long memberId) {
        return dsl.selectFrom(PRODUCT_MAKER)
            .where(PRODUCT_MAKER.PRODUCT_ID.eq(productId))
            .and(PRODUCT_MAKER.MEMBER_ID.eq(memberId))
            .fetchOptionalInto(ProductMaker.class);
    }

    @Override
    public void deleteByProductIdAndMemberId(Long productId, Long memberId) {
        dsl.deleteFrom(PRODUCT_MAKER)
            .where(PRODUCT_MAKER.PRODUCT_ID.eq(productId))
            .and(PRODUCT_MAKER.MEMBER_ID.eq(memberId))
            .execute();
    }
}
