package net.detalk.api.service;


import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.controller.v1.request.UpdateProductPostRequest;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.domain.exception.InvalidPageSizeException;
import net.detalk.api.domain.exception.ProductPostForbiddenException;
import net.detalk.api.domain.exception.ProductPostNotFoundException;
import net.detalk.api.domain.exception.ProductPostSnapshotUpdateException;
import net.detalk.api.support.CursorPageData;
import net.detalk.api.domain.ProductMaker;
import net.detalk.api.domain.ProductPostSnapshotAttachmentFile;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.Product;
import net.detalk.api.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.domain.ProductPost;
import net.detalk.api.domain.ProductPostSnapshot;
import net.detalk.api.domain.ProductPostSnapshotTag;
import net.detalk.api.repository.ProductLinkRepository;
import net.detalk.api.repository.ProductMakerRepository;
import net.detalk.api.repository.ProductPostLastSnapshotRepository;
import net.detalk.api.repository.ProductPostRepository;
import net.detalk.api.repository.ProductPostSnapshotAttachmentFileRepository;
import net.detalk.api.repository.ProductPostSnapshotRepository;
import net.detalk.api.repository.ProductPostSnapshotTagRepository;
import net.detalk.api.repository.ProductRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostService {

    private final ProductRepository productRepository;
    private final ProductPostRepository productPostRepository;
    private final PricingPlanService pricingPlanService;
    private final ProductPostLastSnapshotRepository productPostLastSnapshotRepository;
    private final ProductLinkRepository productLinkRepository;
    private final ProductPostSnapshotAttachmentFileRepository productPostSnapshotAttachmentFileRepository;
    private final ProductMakerRepository productMakerRepository;
    private final TagService tagService;
    private final ProductPostSnapshotTagRepository productPostSnapshotTagRepository;
    private final ProductPostSnapshotRepository productPostSnapshotRepository;
    private final TimeHolder timeHolder;
    private final UUIDGenerator uuidGenerator;

    /**
     * 게시글 생성
     * @param createProductPostRequest 게시글 생성 데이터
     * @return 생성된 게시글 ID
     */
    @Transactional
    public Long create(CreateProductPostRequest createProductPostRequest, Long memberId) {

        Instant now = timeHolder.now();
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
         * 링크 없으면 저장, 있으면 아무것도 안함
         */
        productLinkRepository.findByUrl(productUrl)
            .orElseGet(() -> productLinkRepository.save(productId, productUrl, now));

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

        boolean hasNext = false;
        Long nextPageId = null;

        // +1 했는데, 조회가 되었다면 다음데이터가 있음
        if (result.size() > pageSize) {
            // 마지막 item 추출 (배열이므로 size-1)
            GetProductPostResponse lastItem = result.get(pageSize - 1);
            // 클라이언트가 요청해야할 다음 ID
            nextPageId = lastItem.id();
            hasNext = true;
            // 클라이언트 size 요청 개수만큼 return
            result = result.subList(0, pageSize);
        }

        return new CursorPageData<>(result, nextPageId, hasNext);
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

        boolean hasNext = false;
        Long nextPageId = null;

        // +1 했는데, 조회가 되었다면 다음 데이터가 있음
        if (result.size() > pageSize) {
            // 마지막 item 추출
            GetProductPostResponse lastItem = result.get(pageSize - 1);
            nextPageId = lastItem.id();
            hasNext = true;
            // 클라이언트에 반환할 데이터는 요청한 size만큼
            result = result.subList(0, pageSize);
        }

        return new CursorPageData<>(result, nextPageId, hasNext);
    }


    /**
     * 제품 게시글 상세 조회
     * @param id 조회할 제품 게시글 ID
     * @return 게시글 상세 조회 정보
     */
    @Transactional(readOnly = true)
    public GetProductPostResponse getProductPostDetailsById(Long id) {
        return productPostRepository.findDetailsById(id).orElseThrow(() -> {
            log.error("[getProductPostDetailsById] 제품 게시글 없음 ID: {}", id);
            return new ProductPostNotFoundException(id);
        });
    }


    /**
     * 제품 게시글 업데이트
     *
     * @param postId                수정할 제품 게시글
     * @param updateProductPostRequest 제품 수정 요청 dto
     * @param memberId          요청 회원 postId
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

        boolean hasNext = false;
        Long nextPageId = null;

        if (result.size() > pageSize) {
            GetProductPostResponse lastItem = result.get(pageSize - 1);
            nextPageId = lastItem.id();
            hasNext = true;
            result = result.subList(0, pageSize);
        }
        return new CursorPageData<>(result, nextPageId, hasNext);
    }


    /**
     * 제품 게시글 존재하는지 검증
     * @param id 검증할 제품 게시글 ID
     */
    public void validatePostExists(Long id) {
        if (!productPostRepository.existsById(id)) {
            log.error("[validatePostExists] 게시글이 존재하지 않습니다 : {}" , id);
            throw new ProductPostNotFoundException(id);
        }
    }


    /**
     * 제품 게시글 추천수 증가
     * @param id 증가할 제품 게시글 ID
     */
    public void incrementRecommendCount(Long id) {
        validatePostExists(id);
        productPostRepository.incrementRecommendCount(id);
    }

    /**
     * 페이지 사이즈 검증 1이하라면 에러
     * @param pageSize 검증할 사이즈
     */
    private void validatePageSize(int pageSize) {
        if (pageSize < 1) {
            log.warn("잘못된 페이지 사이즈 요청입니다={}", pageSize);
            throw new InvalidPageSizeException(pageSize);
        }
    }

}
