package net.detalk.api.service;

import static net.detalk.api.support.error.ErrorCode.BAD_REQUEST;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.support.CursorPageData;
import net.detalk.api.domain.ProductMaker;
import net.detalk.api.domain.ProductPostSnapshotAttachmentFile;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.Product;
import net.detalk.api.domain.ProductCreate;
import net.detalk.api.domain.ProductPost;
import net.detalk.api.domain.ProductPostSnapshot;
import net.detalk.api.domain.ProductPostSnapshotTag;
import net.detalk.api.domain.Tag;
import net.detalk.api.repository.PricingPlanRepository;
import net.detalk.api.repository.ProductLinkRepository;
import net.detalk.api.repository.ProductMakerRepository;
import net.detalk.api.repository.ProductPostLastSnapshotRepository;
import net.detalk.api.repository.ProductPostRepository;
import net.detalk.api.repository.ProductPostSnapshotAttachmentFileRepository;
import net.detalk.api.repository.ProductPostSnapshotRepository;
import net.detalk.api.repository.ProductPostSnapshotTagRepository;
import net.detalk.api.repository.ProductRepository;
import net.detalk.api.repository.TagRepository;
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
    private final PricingPlanRepository pricingPlanRepository;
    private final ProductPostLastSnapshotRepository productPostLastSnapshotRepository;
    private final ProductLinkRepository productLinkRepository;
    private final ProductPostSnapshotAttachmentFileRepository productPostSnapshotAttachmentFileRepository;
    private final ProductMakerRepository productMakerRepository;
    private final TagRepository tagRepository;
    private final ProductPostSnapshotTagRepository productPostSnapshotTagRepository;
    private final ProductPostSnapshotRepository productPostSnapshotRepository;
    private final TimeHolder timeHolder;

    // TODO : 작성자 정보 받기, SRP 리팩토링
    @Transactional
    public Long create(ProductCreate productCreate) {

        Instant now = timeHolder.now();

        /*
         * 제품 조회
         * 있다면 재사용
         * 없다면 저장 후 사용
         */
        Product product = productRepository.findByName(productCreate.getName())
            .orElseGet(() -> productRepository.save(productCreate.getName(), now));

        Long productId = product.getId();

        // 게시글 저장
        ProductPost productPost = productPostRepository.save(productCreate.getWriterId(),
            productId, now);


        // 가격 정책 조회
        PricingPlan pricingPlan = pricingPlanRepository.findByName(productCreate.getPricingPlan())
            .orElseThrow(()-> new ApiException(ErrorCode.NOT_FOUND));

        /*
         * 게시글 스냅샷 저장
         */
        ProductPostSnapshot postSnapshot = productPostSnapshotRepository.save(
            productPost.getId(),
            pricingPlan.getId(),
            productCreate.getName(),
            productCreate.getDescription(),
            now
        );
        Long postSnapshotId = postSnapshot.getId();

        /*
         * 게시글 최근 스냅샷 저장
         */
        productPostLastSnapshotRepository.save(productPost.getId(), postSnapshotId);

        /*
         * 링크 없으면 저장, 있으면 아무것도 안함
         */
        productLinkRepository.findByUrl(productCreate.getUrl())
            .orElseGet(() -> productLinkRepository.save(productId, productCreate.getUrl(), now));

        /*
         * 이미지 파일 시퀀스 설정 및 스냅샷 저장
         */
        List<Long> imageIds = productCreate.getImageIds();

        for(int sequence = 0; sequence < imageIds.size(); sequence++) {
            Long attachmentFileId = imageIds.get(sequence);

            ProductPostSnapshotAttachmentFile attachmentFile = ProductPostSnapshotAttachmentFile.builder()
                .snapshotId(postSnapshotId)
                .attachmentFileId(attachmentFileId)
                .sequence(sequence)
                .build();

            productPostSnapshotAttachmentFileRepository.save(attachmentFile);
        }

        /*
         * 메이커 여부
         */
        if (productCreate.isMaker()) {
            ProductMaker maker = ProductMaker.builder()
                .productId(productId)
                .memberId(productCreate.getWriterId())
                .createdAt(timeHolder.now())
                .build();
            productMakerRepository.save(maker);
        }

        /*
         * 태그 있다면 재사용
         * 없다면 저장
         */
        List<String> tags = productCreate.getTags();

        List<ProductPostSnapshotTag> snapshotTags = tags.stream()
            .map(this::getOrCreateTag)
            .map(tag -> ProductPostSnapshotTag.builder()
                .postId(postSnapshotId)
                .tagId(tag.getId())
                .build())
            .toList();

        productPostSnapshotTagRepository.saveAll(snapshotTags);

        return productPost.getId();
    }

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

    @Transactional(readOnly = true)
    public GetProductPostResponse getProductPostById(Long id) {
        return productPostRepository.findById(id).orElseThrow(() -> {
            log.warn("[getProductPostById] 제품 게시글 없음 ID: {}", id);
            return new ApiException(ErrorCode.NOT_FOUND);
        });
    }


    private Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
    }
}
