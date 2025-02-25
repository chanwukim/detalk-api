package net.detalk.api.post.service;


import java.time.Instant;
import java.util.List;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.post.controller.v1.request.UpdateProductPostRequest;
import net.detalk.api.post.controller.v1.response.GetProductPostResponse;
import net.detalk.api.plan.service.PricingPlanService;
import net.detalk.api.post.repository.ProductPostLastSnapshotRepository;
import net.detalk.api.post.repository.ProductPostLinkRepository;
import net.detalk.api.post.repository.ProductPostRepository;
import net.detalk.api.post.repository.ProductPostSnapshotAttachmentFileRepository;
import net.detalk.api.post.repository.ProductPostSnapshotRepository;
import net.detalk.api.post.repository.ProductPostSnapshotTagRepository;
import net.detalk.api.product.domain.ProductLink;
import net.detalk.api.post.domain.exception.DuplicateCreatePostException;
import net.detalk.api.support.error.InvalidPageSizeException;
import net.detalk.api.post.domain.exception.InvalidRecommendCountRequest;
import net.detalk.api.post.domain.exception.ProductPostForbiddenException;
import net.detalk.api.post.domain.exception.ProductPostNotFoundException;
import net.detalk.api.post.domain.exception.ProductPostSnapshotUpdateException;
import net.detalk.api.product.repository.ProductLinkRepository;
import net.detalk.api.product.repository.ProductMakerRepository;
import net.detalk.api.product.repository.ProductRepository;
import net.detalk.api.tag.service.TagService;
import net.detalk.api.support.paging.CursorPageData;
import net.detalk.api.product.domain.ProductMaker;
import net.detalk.api.post.domain.ProductPostSnapshotAttachmentFile;
import net.detalk.api.plan.domain.PricingPlan;
import net.detalk.api.product.domain.Product;
import net.detalk.api.post.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.post.domain.ProductPost;
import net.detalk.api.post.domain.ProductPostSnapshot;
import net.detalk.api.post.domain.ProductPostSnapshotTag;
import net.detalk.api.support.util.TimeHolder;
import net.detalk.api.support.util.UUIDGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostService {

    /**
     * Product
     */
    private final ProductRepository productRepository;
    private final ProductLinkRepository productLinkRepository;
    private final ProductMakerRepository productMakerRepository;

    /**
     * Product-Post
     */
    private final ProductPostRepository productPostRepository;
    private final ProductPostLastSnapshotRepository productPostLastSnapshotRepository;
    private final ProductPostSnapshotAttachmentFileRepository productPostSnapshotAttachmentFileRepository;
    private final ProductPostSnapshotTagRepository productPostSnapshotTagRepository;
    private final ProductPostSnapshotRepository productPostSnapshotRepository;
    private final ProductPostLinkRepository productPostLinkRepository;
    private final ProductPostIdempotentService idempotentService;

    /**
     * PricingPlan
     */
    private final PricingPlanService pricingPlanService;

    /**
     * Tag
     */
    private final TagService tagService;

    /**
     * ETC
     */
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    @Transactional
    public Long create(CreateProductPostRequest createProductPostRequest, Long memberId) {

        Instant now = timeHolder.now();
        UUID idempotentKey = uuidGenerator.fromString(createProductPostRequest.idempotentKey());
        boolean isFirstTime = idempotentService.insertIdempotentKey(idempotentKey, now);

        if (!isFirstTime) {
            log.info("같은 게시글 여러번 요청 uuid={}", idempotentKey);
            throw new DuplicateCreatePostException();
        }

        String productUrl = createProductPostRequest.url();
        String productName = createProductPostRequest.name();

        /*
         * 제품 조회
         * 있다면 재사용
         * 없다면 저장 후 사용
         */
        Product product = productRepository.findByName(productName)
            .orElseGet(() -> productRepository.save(createProductPostRequest.name(), now));

        Long productId = product.getId();

        /*
         * 게시글 저장
         */
        ProductPost newProductPost = productPostRepository.save(memberId, productId, now);
        Long newProductPostId = newProductPost.getId();

        /*
         * 요청 가격 정책 조회
         */
        PricingPlan pricingPlan = pricingPlanService.findByName(
            createProductPostRequest.pricingPlan());

        /*
         * 게시글 스냅샷 저장
         */
        ProductPostSnapshot postSnapshot = productPostSnapshotRepository.save(
            ProductPostSnapshot.builder()
                .postId(newProductPostId)
                .pricingPlanId(pricingPlan.getId())
                .title(productName)
                .description(createProductPostRequest.description())
                .createdAt(timeHolder.now()).build(
                ));
        Long postSnapshotId = postSnapshot.getId();

        /*
         * 게시글 최근 스냅샷 저장
         */
        productPostLastSnapshotRepository.save(newProductPostId, postSnapshotId);

        /*
         * 링크 없으면 저장, 있으면 링크 그대로 가져옴 -> 게시글과 링크 연관관계 맺기
         */
        Optional<ProductLink> optionalLink = productLinkRepository.findByUrl(productUrl);
        ProductLink productLink = optionalLink.orElseGet(
            () -> productLinkRepository.save(productId, productUrl, now)
        );
        productPostLinkRepository.save(newProductPostId, productLink.getId());


        /*
         * 이미지 파일 시퀀스 설정 및 스냅샷 저장
         */
        List<String> imageIds = createProductPostRequest.imageIds();

        for(int sequence = 0; sequence < imageIds.size(); sequence++) {
            String attachmentFileId = imageIds.get(sequence);
            ProductPostSnapshotAttachmentFile attachmentFile = ProductPostSnapshotAttachmentFile.create(
                postSnapshotId, uuidGenerator.fromString(attachmentFileId), sequence);
            productPostSnapshotAttachmentFileRepository.save(attachmentFile);
        }

        /*
         * 메이커 여부
         */
        if (createProductPostRequest.isMaker()) {
            ProductMaker maker = ProductMaker.create(productId, memberId, timeHolder);
            productMakerRepository.save(maker);
        }

        /*
         * 태그 있다면 재사용
         * 없다면 저장
         */
        List<String> tags = createProductPostRequest.tags();

        List<ProductPostSnapshotTag> snapshotTags = tags.stream()
            .map(tagService::getOrCreateTag)
            .map(tag -> ProductPostSnapshotTag.builder()
                .postId(postSnapshotId)
                .tagId(tag.getId())
                .build())
            .toList();

        productPostSnapshotTagRepository.saveAll(snapshotTags);

        return newProductPostId;
    }

    /**
     * 제품 게시글 목록 커서 조회
     * @param pageSize 요청 item 개수
     * @param nextId 다음 페이지 item id
     * @return 제품 게시글 커서 페이징 목록
     */
    public CursorPageData<GetProductPostResponse> getProductPosts(int pageSize, Long nextId) {

        validatePageSize(pageSize);

        // hasNext 판별하기 위해 pageSize + 1
        List<GetProductPostResponse> result = productPostRepository.findProductPosts(
            pageSize + 1, nextId);

        return createCursorPage(result, pageSize);
    }

    /**
     * 회원ID로 제품 게시글 목록 조회
     *
     * @param memberId 조회하려는 회원 ID
     * @param pageSize 요청 item 개수
     * @param nextId   다음 페이지 item id
     * @return 회원 제품 게시글 커서 페이징 목록
     */
    public CursorPageData<GetProductPostResponse> getProductPostsByMemberId(Long memberId,
        int pageSize, Long nextId) {

        validatePageSize(pageSize);

        // hasNext 판별하기 위해 pageSize + 1
        List<GetProductPostResponse> result = productPostRepository.findProductPostsByMemberId(
            memberId, pageSize + 1, nextId);

        return createCursorPage(result, pageSize);
    }


    /**
     * 제품 게시글 상세 조회
     * @param id 조회할 제품 게시글 ID
     * @return 게시글 상세 조회 정보
     */
    @Transactional(readOnly = true)
    public GetProductPostResponse getProductPostDetailsById(Long id) {
        return productPostRepository.findDetailsById(id).orElseThrow(() -> {
            log.info("[getProductPostDetailsById] 제품 게시글 없음 ID: {}", id);
            return new ProductPostNotFoundException(id);
        });
    }


    /**
     * Updates an existing product post with new information.
     *
     * @param postId The unique identifier of the product post to be updated
     * @param updateProductPostRequest Data transfer object containing updated product post details
     * @param memberId The identifier of the member requesting the update
     * @return The ID of the newly created product post snapshot
     * @throws ProductPostNotFoundException If the specified product post does not exist
     * @throws ProductPostForbiddenException If the requesting member is not the author of the post
     * @throws ProductPostSnapshotUpdateException If updating the last snapshot fails
     */
    public Long update(Long postId, UpdateProductPostRequest updateProductPostRequest, Long memberId) {

        Instant now = timeHolder.now();

        // 1. 게시글 존재
        ProductPost productPost = productPostRepository.findById(postId).orElseThrow(() -> {
            log.error("[update] 제품 게시글을 찾지 못했습니다 postId={}", postId);
            return new ProductPostNotFoundException(postId);
        });

        // 작성자 검증
        if (!productPost.isAuthor(memberId)) {
            log.error("[update] 작성자와 요청자가 다릅니다 memberId={}", memberId);
            throw new ProductPostForbiddenException();
        }

        // 2. 제품 조회 또는 재사용
        String productName = updateProductPostRequest.name();
        Product product = productRepository.findByName(productName)
            .orElseGet(() -> productRepository.save(updateProductPostRequest.name(), now));
        Long productId = product.getId();

        // 3. 가격 정책 조회
        PricingPlan pricingPlan = pricingPlanService.findByName(
            updateProductPostRequest.pricingPlan());

        // 4. 새 스냅샷 생성
        ProductPostSnapshot newSnapshot = productPostSnapshotRepository.save(
            ProductPostSnapshot.builder()
                .postId(postId)
                .pricingPlanId(pricingPlan.getId())
                .title(productName)
                .description(updateProductPostRequest.description())
                .createdAt(now)
                .build()
        );

        Long newSnapshotId = newSnapshot.getId();

        // 5. 새 스냅샷 태그 추가
        List<String> tags = updateProductPostRequest.tags();
        List<ProductPostSnapshotTag> snapshotTags = tags.stream()
            .map(tagService::getOrCreateTag)
            .map(tag -> ProductPostSnapshotTag.builder()
                .postId(newSnapshotId)
                .tagId(tag.getId())
                .build())
            .toList();

        productPostSnapshotTagRepository.saveAll(snapshotTags);

        // 6. 링크 업데이트
        String url = updateProductPostRequest.url();
        if (url != null && !url.isEmpty()) {
            productLinkRepository.findByUrl(url)
                .orElseGet(() -> productLinkRepository.save(productId, url, now));
        }

        // 7. 이미지 업데이트
        List<String> imageIds = updateProductPostRequest.imageIds();
        for (int sequence = 0; sequence < imageIds.size(); sequence++) {
            String attachmentFileId = imageIds.get(sequence);

            ProductPostSnapshotAttachmentFile attachmentFile = ProductPostSnapshotAttachmentFile.create(
                newSnapshotId, uuidGenerator.fromString(attachmentFileId), sequence);

            productPostSnapshotAttachmentFileRepository.save(attachmentFile);
        }

        // 8. 제품 메이커 확인 (메이커 요청일 경우)
        if (updateProductPostRequest.isMaker()) {
            productMakerRepository.findByProductIdAndMemberId(productId, memberId)
                .orElseGet(() -> {
                    // 새 메이커 요청일 경우
                    log.info("[update] 메이커 생성: productId={}, memberId={}", productId, memberId);
                    ProductMaker maker = ProductMaker.create(productId, memberId, timeHolder);
                    return productMakerRepository.save(maker);
                });
        }else{
            // 메이커 요청을 하지 않았을 경우
            // 이미 메이커일 경우에만 메이커 삭제
            log.info("[update] 메이커 삭제 시도: productId={}, memberId={}", productId, memberId);
            productMakerRepository.deleteByProductIdAndMemberId(productId, memberId);
        }

        // 9. 최근 스냅샷 업데이트
        int updateResult = productPostLastSnapshotRepository.update(postId, newSnapshot);
        if (updateResult == 0) {
            log.error("[update] 게시글 수정에 실패했습니다. postId={}, newSnapshotId={}", postId,
                newSnapshot.getId());
            throw new ProductPostSnapshotUpdateException(postId, newSnapshot.getId());
        }

        return newSnapshotId;
    }


    public CursorPageData<GetProductPostResponse> getRecommendedPostsByMemberId(Long memberId,
        int pageSize, Long nextId) {

        validatePageSize(pageSize);

        List<GetProductPostResponse> result =
            productPostRepository.findRecommendedPostsByMemberId(memberId, pageSize + 1, nextId);

        return createCursorPage(result, pageSize);
    }

    public CursorPageData<GetProductPostResponse> getProductPostsByTags(int pageSize, Long nextId,
        List<String> tags) {

        validatePageSize(pageSize);

        List<GetProductPostResponse> result = productPostRepository.findProductPostsByTags(
            pageSize, nextId, tags);

        return createCursorPage(result, pageSize);
    }


    /**
     * 제품 게시글 존재하는지 검증
     * @param id 검증할 제품 게시글 ID
     */
    public void validatePostExists(Long id) {
        if (!productPostRepository.existsById(id)) {
            log.info("[validatePostExists] 게시글이 존재하지 않습니다 : {}" , id);
            throw new ProductPostNotFoundException(id);
        }
    }


    /**
     * 제품 게시글 추천수 증가
     * @param id   추천할 제품 게시글 ID
     * @param count 추천 수
     */
    public void incrementRecommendCount(Long id, int count) {
        if (count <= 0) {
            log.info("추천 수는 양수여야 합니다. count={}", count);
            throw new InvalidRecommendCountRequest(count);
        }
        productPostRepository.incrementRecommendCount(id, count);
    }

    /**
     * 페이지 사이즈 검증 1이하라면 에러
     * @param pageSize 검증할 사이즈
     */
    private void validatePageSize(int pageSize) {
        if (pageSize < 1) {
            log.info("잘못된 페이지 사이즈 요청입니다={}", pageSize);
            throw new InvalidPageSizeException(pageSize);
        }
    }

    /**
     * 커서 기반 페이징 처리
     *
     * @param result   DB에서 조회한 결과 (pageSize + 1개)
     * @param pageSize 페이지당 표시할 아이템 수
     * @return CursorPageData 페이징 결과
     */
    private CursorPageData<GetProductPostResponse> createCursorPage(
        List<GetProductPostResponse> result, int pageSize) {

        // DB 조회 데이터가 없을 경우, 빈 페이지 반환
        if (result == null) {
            return new CursorPageData<>(List.of(), null, false);
        }

        boolean hasNext = false;
        Long nextPageId = null;

        // 요청+1을 조회했는데, result.size 가 요청보다 클 경우
        // 다음 데이터가 있는거임
        if (result.size() > pageSize) {
            GetProductPostResponse lastItem = result.get(pageSize - 1);
            nextPageId = lastItem.id();
            hasNext = true;
            result = result.subList(0, pageSize);
        }
        return new CursorPageData<>(result, nextPageId, hasNext);
    }

}
