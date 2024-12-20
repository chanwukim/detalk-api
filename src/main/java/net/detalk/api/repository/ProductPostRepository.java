package net.detalk.api.repository;

import static net.detalk.jooq.tables.JAttachmentFile.ATTACHMENT_FILE;
import static net.detalk.jooq.tables.JMemberProfile.MEMBER_PROFILE;
import static net.detalk.jooq.tables.JPricingPlan.PRICING_PLAN;
import static net.detalk.jooq.tables.JProductLink.PRODUCT_LINK;
import static net.detalk.jooq.tables.JProductMaker.PRODUCT_MAKER;
import static net.detalk.jooq.tables.JProductPost.PRODUCT_POST;
import static net.detalk.jooq.tables.JProductPostLastSnapshot.PRODUCT_POST_LAST_SNAPSHOT;
import static net.detalk.jooq.tables.JProductPostSnapshot.PRODUCT_POST_SNAPSHOT;
import static net.detalk.jooq.tables.JProductPostSnapshotAttachmentFile.PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE;
import static net.detalk.jooq.tables.JProductPostSnapshotTag.PRODUCT_POST_SNAPSHOT_TAG;
import static net.detalk.jooq.tables.JTag.TAG;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse.Media;
import net.detalk.api.domain.ProductPost;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductPostRepository {

    private final DSLContext dsl;

    public ProductPost save(Long writerId, Long productId, Instant now) {
        return dsl.insertInto(PRODUCT_POST)
            .set(PRODUCT_POST.WRITER_ID, writerId)
            .set(PRODUCT_POST.PRODUCT_ID, productId)
            .set(PRODUCT_POST.CREATED_AT, now)
            .set(PRODUCT_POST.RECOMMEND_COUNT, 0L)
            .returning()
            .fetchOneInto(ProductPost.class);
    }

    public Optional<ProductPost> findById(Long id) {
        return dsl.selectFrom(PRODUCT_POST)
            .where(PRODUCT_POST.ID.eq(id))
            .fetchOptionalInto(ProductPost.class);
    }

    public Optional<ProductPost> findByProductId(Long productId) {
        return dsl.selectFrom(PRODUCT_POST)
            .where(PRODUCT_POST.PRODUCT_ID.eq(productId))
            .fetchOptionalInto(ProductPost.class);
    }

    public Optional<GetProductPostResponse> findDetailsById(Long id) {

        var result = dsl.select(
                PRODUCT_POST.ID,
                MEMBER_PROFILE.NICKNAME,
                MEMBER_PROFILE.USERHANDLE.as("userHandle"),
                PRODUCT_POST_SNAPSHOT.CREATED_AT.as("createdAt"),
                DSL.when(PRODUCT_MAKER.ID.isNotNull(), true).otherwise(false).as("isMaker"),
                ATTACHMENT_FILE.URL.as("avatarUrl"),
                PRODUCT_POST_SNAPSHOT.TITLE,
                PRODUCT_POST_SNAPSHOT.DESCRIPTION,
                PRICING_PLAN.NAME.as("pricingPlan"),
                DSL.arrayAgg(TAG.NAME).as("tags"),
                PRODUCT_POST.RECOMMEND_COUNT.as("recommendCount"),
                PRODUCT_POST_SNAPSHOT.ID.as("snapshotId"),
                DSL.arrayAggDistinct(PRODUCT_LINK.URL).as("productUrls")
            )
            .from(PRODUCT_POST)
            .join(PRODUCT_POST_LAST_SNAPSHOT)
            .on(PRODUCT_POST.ID.eq(PRODUCT_POST_LAST_SNAPSHOT.POST_ID))
            .join(PRODUCT_POST_SNAPSHOT)
            .on(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
            .join(PRICING_PLAN)
            .on(PRODUCT_POST_SNAPSHOT.PRICING_PLAN_ID.eq(PRICING_PLAN.ID))
            .leftJoin(PRODUCT_POST_SNAPSHOT_TAG)
            .on(PRODUCT_POST_SNAPSHOT_TAG.POST_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
            .leftJoin(TAG)
            .on(TAG.ID.eq(PRODUCT_POST_SNAPSHOT_TAG.TAG_ID))
            .leftJoin(MEMBER_PROFILE)
            .on(MEMBER_PROFILE.MEMBER_ID.eq(PRODUCT_POST.WRITER_ID))
            .leftJoin(PRODUCT_MAKER)
            .on(PRODUCT_MAKER.PRODUCT_ID.eq(PRODUCT_POST.PRODUCT_ID))
            .leftJoin(ATTACHMENT_FILE)
            .on(ATTACHMENT_FILE.ID.eq(MEMBER_PROFILE.AVATAR_ID))
            .leftJoin(PRODUCT_LINK)
            .on(PRODUCT_LINK.PRODUCT_ID.eq(PRODUCT_POST.PRODUCT_ID))
            .where(PRODUCT_POST.ID.eq(id))
            .groupBy(
                PRODUCT_POST.ID,
                MEMBER_PROFILE.NICKNAME,
                MEMBER_PROFILE.USERHANDLE,
                PRODUCT_POST_SNAPSHOT.CREATED_AT,
                PRODUCT_MAKER.ID,
                ATTACHMENT_FILE.URL,
                PRODUCT_POST_SNAPSHOT.TITLE,
                PRODUCT_POST_SNAPSHOT.DESCRIPTION,
                PRICING_PLAN.NAME,
                PRODUCT_POST.RECOMMEND_COUNT,
                PRODUCT_POST_SNAPSHOT.ID
            )
            .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        Long snapshotId = result.get("snapshotId", Long.class);
        List<Media> images = Collections.emptyList();

        if (snapshotId != null) {
            var imagesResult = dsl.select(
                    PRODUCT_POST.ID.as("postId"),
                    PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE.as("sequence"),
                    ATTACHMENT_FILE.URL.as("imageUrl")
                )
                .from(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE)
                .join(ATTACHMENT_FILE)
                    .on(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.ATTACHMENT_FILE_ID.eq(ATTACHMENT_FILE.ID))
                .join(PRODUCT_POST_SNAPSHOT)
                    .on(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
                .join(PRODUCT_POST_LAST_SNAPSHOT)
                    .on(PRODUCT_POST_SNAPSHOT.ID.eq(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID))
                .join(PRODUCT_POST)
                    .on(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(PRODUCT_POST.ID))
                .where(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID.eq(snapshotId))
                .orderBy(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE.asc())
                .fetch();

            images = imagesResult.stream()
                .map(record -> new Media(
                    record.get("imageUrl", String.class),
                    record.get("sequence", Integer.class)
                ))
                .toList();
        }

        String[] tagsArr = result.get("tags", String[].class);
        List<String> tags = List.of(tagsArr);

        String[] productUrlsArr = result.get("productUrls", String[].class);
        List<String> productUrls = List.of(productUrlsArr);

        return Optional.of(new GetProductPostResponse(
            result.get(PRODUCT_POST.ID, Long.class),
            result.get("nickname", String.class),
            result.get("userHandle", String.class),
            result.get("createdAt", Instant.class),
            result.get("isMaker", Boolean.class),
            result.get("avatarUrl", String.class),
            result.get(PRODUCT_POST_SNAPSHOT.TITLE, String.class),
            result.get(PRODUCT_POST_SNAPSHOT.DESCRIPTION, String.class),
            result.get("pricingPlan", String.class),
            result.get("recommendCount", Integer.class),
            tags,
            images,
            productUrls
        ));
    }


    public List<GetProductPostResponse> findProductPosts(int pageSize, Long nextId) {

        // nextId null인 경우, 가장 최신 데이터를 조회
        Condition condition = DSL.trueCondition();

        //
        if (nextId != null) {
            condition = PRODUCT_POST.ID.lt(nextId);
        }

        // 1. 게시글 목록 조회
        var result = dsl.select(
                PRODUCT_POST.ID,
                MEMBER_PROFILE.NICKNAME,
                MEMBER_PROFILE.USERHANDLE.as("userHandle"),
                PRODUCT_POST_SNAPSHOT.CREATED_AT.as("createdAt"),
                DSL.when(PRODUCT_MAKER.ID.isNotNull(), true).otherwise(false).as("isMaker"),
                ATTACHMENT_FILE.URL.as("avatarUrl"),
                PRODUCT_POST_SNAPSHOT.TITLE,
                PRODUCT_POST_SNAPSHOT.DESCRIPTION,
                PRICING_PLAN.NAME.as("pricingPlan"),
                DSL.arrayAgg(TAG.NAME).as("tags"),
                PRODUCT_POST.RECOMMEND_COUNT.as("recommendCount"),
                PRODUCT_POST_SNAPSHOT.ID.as("snapshotId"),
                DSL.arrayAggDistinct(PRODUCT_LINK.URL).as("productUrls")
            )
            .from(PRODUCT_POST)
            .join(PRODUCT_POST_LAST_SNAPSHOT)
                .on(PRODUCT_POST.ID.eq(PRODUCT_POST_LAST_SNAPSHOT.POST_ID))
            .join(PRODUCT_POST_SNAPSHOT)
                .on(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
            .join(PRICING_PLAN)
                .on(PRODUCT_POST_SNAPSHOT.PRICING_PLAN_ID.eq(PRICING_PLAN.ID))
            .leftJoin(PRODUCT_POST_SNAPSHOT_TAG)
                .on(PRODUCT_POST_SNAPSHOT_TAG.POST_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
            .leftJoin(TAG)
                .on(TAG.ID.eq(PRODUCT_POST_SNAPSHOT_TAG.TAG_ID))
            .leftJoin(MEMBER_PROFILE)
                .on(MEMBER_PROFILE.MEMBER_ID.eq(PRODUCT_POST.WRITER_ID))
            .leftJoin(PRODUCT_MAKER)
                .on(PRODUCT_MAKER.PRODUCT_ID.eq(PRODUCT_POST.PRODUCT_ID))
            .leftJoin(ATTACHMENT_FILE)
                .on(ATTACHMENT_FILE.ID.eq(MEMBER_PROFILE.AVATAR_ID))
            .leftJoin(PRODUCT_LINK)
                .on(PRODUCT_LINK.PRODUCT_ID.eq(PRODUCT_POST.PRODUCT_ID))
            .where(condition)
            .groupBy(
                PRODUCT_POST.ID,
                MEMBER_PROFILE.NICKNAME,
                MEMBER_PROFILE.USERHANDLE,
                PRODUCT_POST_SNAPSHOT.CREATED_AT,
                PRODUCT_MAKER.ID,
                ATTACHMENT_FILE.URL,
                PRODUCT_POST_SNAPSHOT.TITLE,
                PRODUCT_POST_SNAPSHOT.DESCRIPTION,
                PRICING_PLAN.NAME,
                PRODUCT_POST.RECOMMEND_COUNT,
                PRODUCT_POST_SNAPSHOT.ID
            )
            .orderBy(PRODUCT_POST.CREATED_AT.desc(), PRODUCT_POST.ID.desc())
            .limit(pageSize)
            .fetch();

        // 첫 번째 쿼리 결과에서 스냅샷 ID 추출
        List<Long> snapshotIds = result.getValues("snapshotId", Long.class);
        Map<Long, List<Media>> imagesMap;

        // 2. 게시글 정보에 대한 썸네일 사진, 사진 sequence 조회
        if (!snapshotIds.isEmpty()) {
            var imagesResult = dsl.select(
                    PRODUCT_POST.ID.as("postId"),
                    PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE.as("sequence"),
                    ATTACHMENT_FILE.URL.as("imageUrl")
                )
                .from(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE)
                .join(ATTACHMENT_FILE)
                    .on(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.ATTACHMENT_FILE_ID.eq(ATTACHMENT_FILE.ID))
                .join(PRODUCT_POST_SNAPSHOT)
                    .on(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID.eq(PRODUCT_POST_SNAPSHOT.ID))
                .join(PRODUCT_POST_LAST_SNAPSHOT)
                    .on(PRODUCT_POST_SNAPSHOT.ID.eq(PRODUCT_POST_LAST_SNAPSHOT.SNAPSHOT_ID))
                .join(PRODUCT_POST)
                    .on(PRODUCT_POST_LAST_SNAPSHOT.POST_ID.eq(PRODUCT_POST.ID))
                .where(PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SNAPSHOT_ID.in(snapshotIds))
                .orderBy(PRODUCT_POST.ID.asc(), PRODUCT_POST_SNAPSHOT_ATTACHMENT_FILE.SEQUENCE.asc())
                .fetch();

            /*
             * K : postId
             * V : List<Media>
             *
             * [Media(url=fooUrl1, sequence=0), Media(url=fooUrl2, sequence=1)]
             */
            imagesMap = imagesResult.stream()
                .collect(Collectors.groupingBy(
                    record -> record.get("postId", Long.class),
                    Collectors.mapping(
                        record -> new Media(
                            record.get("imageUrl", String.class),
                            record.get("sequence", Integer.class)
                        ),
                       Collectors.toList()
                    )
                ));
        } else {
            imagesMap = new HashMap<>();
        }

        return result.map(record -> {

            String[] tagsArr = record.get("tags", String[].class);
            List<String> tags = List.of(tagsArr);

            Long postId = record.get(PRODUCT_POST.ID);
            List<Media> images = imagesMap.getOrDefault(postId, List.of());

            String[] productUrlsArr = record.get("productUrls", String[].class);
            List<String> productUrls = List.of(productUrlsArr);
            return new GetProductPostResponse(
                record.get("id", Long.class),
                record.get("nickname", String.class),
                record.get("userHandle", String.class),
                record.get("createdAt", Instant.class),
                record.get("isMaker", Boolean.class),
                record.get("avatarUrl", String.class),
                record.get("title", String.class),
                record.get("description", String.class),
                record.get("pricingPlan", String.class),
                record.get("recommendCount", Integer.class),
                tags,
                images,
                productUrls);
        });
    }

    public boolean existsById(Long id) {
        Integer count = dsl.selectCount()
            .from(PRODUCT_POST)
            .where(PRODUCT_POST.ID.eq(id))
            .fetchOne(0, Integer.class);
        return count != null && count > 0;
    }

    public void incrementRecommendCount(Long postId) {
        dsl.update(PRODUCT_POST)
            .set(PRODUCT_POST.RECOMMEND_COUNT, PRODUCT_POST.RECOMMEND_COUNT.plus(1))
            .where(PRODUCT_POST.ID.eq(postId))
            .execute();
    }
}
