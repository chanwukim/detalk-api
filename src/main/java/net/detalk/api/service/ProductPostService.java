package net.detalk.api.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    // TODO : 검증 로직 구현, S3 이미지 구현, 리턴 구현
    @Transactional
    public void create(ProductCreate productCreate) {

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
            .map(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(
                        Tag.builder()
                            .name(tagName)
                            .build())
                    );

                return ProductPostSnapshotTag.builder()
                    .postId(postSnapshotId)
                    .tagId(tag.getId())
                    .build();

            })
            .toList();

        productPostSnapshotTagRepository.saveAll(snapshotTags);
    }


}
