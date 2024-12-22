package net.detalk.api.service;

import static net.detalk.api.support.error.ErrorCode.BAD_REQUEST;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.support.CursorPageData;
import net.detalk.api.domain.ProductMaker;
import net.detalk.api.domain.ProductPostSnapshotAttachmentFile;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.Product;
import net.detalk.api.controller.v1.request.ProductPostCreate;
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

    /**
     * 게시글 생성
     * @param productPostCreate 게시글 생성 데이터
     * @return 생성된 게시글 ID
     */
    @Transactional
    public Long create(ProductPostCreate productPostCreate, Long memberId) {

        Instant now = timeHolder.now();
        String productUrl = productPostCreate.url();
        String productName = productPostCreate.name();

        /*
         * 제품 조회
         * 있다면 재사용
         * 없다면 저장 후 사용
         */
        Product product = productRepository.findByName(productName)
            .orElseGet(() -> productRepository.save(productPostCreate, now));

        Long productId = product.getId();

        /*
         * 게시글 저장
         */
        ProductPost newProductPost = productPostRepository.save(memberId, productId, now);
        Long newProductPostId = newProductPost.getId();

        /*
         * 요청 가격 정책 조회
         */
        PricingPlan pricingPlan = pricingPlanService.findById(productPostCreate.pricingPlan());

        /*
         * 게시글 스냅샷 저장
         */
        ProductPostSnapshot postSnapshot = productPostSnapshotRepository.save(
            ProductPostSnapshot.builder()
                .postId(newProductPostId)
                .pricingPlanId(pricingPlan.getId())
                .title(productName)
                .description(productPostCreate.description())
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
        List<String> imageIds = productPostCreate.imageIds();

        for(int sequence = 0; sequence < imageIds.size(); sequence++) {
            String attachmentFileId = imageIds.get(sequence);
            ProductPostSnapshotAttachmentFile attachmentFile = ProductPostSnapshotAttachmentFile.create(
                postSnapshotId, UUID.fromString(attachmentFileId), sequence);
            productPostSnapshotAttachmentFileRepository.save(attachmentFile);
        }

        /*
         * 메이커 여부
         */
        if (productPostCreate.isMaker()) {
            ProductMaker maker = ProductMaker.create(productId, memberId, timeHolder);
            productMakerRepository.save(maker);
        }

        /*
         * 태그 있다면 재사용
         * 없다면 저장
         */
        List<String> tags = productPostCreate.tags();

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

        // 요청 size가 1보다 작을 경우 예외 발생
        if (pageSize < 1) {
            log.warn("잘못된 페이지 사이즈 요청입니다={}", pageSize);
            throw new ApiException(BAD_REQUEST);
        }

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
     * 제품 게시글 상세 조회
     * @param id 조회할 제품 게시글 ID
     * @return 게시글 상세 조회 정보
     */
    @Transactional(readOnly = true)
    public GetProductPostResponse getProductPostDetailsById(Long id) {
        return productPostRepository.findDetailsById(id).orElseThrow(() -> {
            log.error("[getProductPostDetailsById] 제품 게시글 없음 ID: {}", id);
            return new ApiException(ErrorCode.NOT_FOUND);
        });
    }

    /**
     * 제품 게시글 존재하는지 검증
     * @param id 검증할 제품 게시글 ID
     */
    public void validatePostExists(Long id) {
        if (!productPostRepository.existsById(id)) {
            log.error("[addRecommendation] 게시글이 존재하지 않습니다 : {}" , id);
            throw new ApiException(ErrorCode.NOT_FOUND);
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

}
