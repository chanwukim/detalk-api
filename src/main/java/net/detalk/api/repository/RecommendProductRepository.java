package net.detalk.api.repository;

import static net.detalk.jooq.tables.JRecommendProduct.*;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.RecommendProduct;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendProductRepository {

    private final DSLContext dsl;

    /**
     * 특정 회원이 특정 게시글에 이미 추천했는지 확인
     *
     * @param memberId       회원 ID
     * @param recommendId    추천 이유 ID
     * @param productPostId  게시글 ID
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    public boolean isAlreadyRecommended(Long memberId, Long recommendId,
        Long productPostId) {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(RECOMMEND_PRODUCT)
                .where(RECOMMEND_PRODUCT.MEMBER_ID.eq(memberId))
                .and(RECOMMEND_PRODUCT.RECOMMEND_ID.eq(recommendId))
                .and(RECOMMEND_PRODUCT.PRODUCT_POST_ID.eq(productPostId))
        );
    }

    /**
     * 추천 관계 저장
     *
     * @param recommendId    추천 이유 ID
     * @param productPostId  게시글 ID
     * @param memberId       회원 ID
     * @param now            현재 시각
     * @return 저장된 RecommendProduct 객체
     */
    public RecommendProduct save(Long recommendId, Long productPostId, Long memberId, Instant now) {
        return dsl.insertInto(RECOMMEND_PRODUCT)
            .set(RECOMMEND_PRODUCT.RECOMMEND_ID, recommendId)
            .set(RECOMMEND_PRODUCT.PRODUCT_POST_ID, productPostId)
            .set(RECOMMEND_PRODUCT.MEMBER_ID, memberId)
            .set(RECOMMEND_PRODUCT.CREATED_AT, now)
            .returning()
            .fetchOneInto(RecommendProduct.class);
    }

}