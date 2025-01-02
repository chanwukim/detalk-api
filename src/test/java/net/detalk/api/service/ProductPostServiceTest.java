package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.detalk.api.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.Product;
import net.detalk.api.domain.ProductLink;
import net.detalk.api.domain.ProductPost;
import net.detalk.api.domain.ProductPostSnapshot;
import net.detalk.api.domain.Tag;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductPostServiceTest {

    /**
     * target test class
     */
    private ProductPostService productPostService;

    /**
     * mock repository, service
     */
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPostRepository postRepository;
    @Mock
    private ProductPostLastSnapshotRepository postLastSnapshotRepository;
    @Mock
    private ProductLinkRepository linkRepository;
    @Mock
    private ProductPostSnapshotAttachmentFileRepository snapshotAttachmentFileRepository;
    @Mock
    private ProductMakerRepository makerRepository;
    @Mock
    private ProductPostSnapshotTagRepository postSnapshotTagRepository;
    @Mock
    private ProductPostSnapshotRepository postSnapshotRepository;
    @Mock
    private TagService tagService;
    @Mock
    private PricingPlanService planService;

    /**
     * fake random classes
     */
    private TimeHolder timeHolder = new FakeTimeHolder(Instant.parse("2025-01-01T12:00:00Z"));
    private UUIDGenerator uuidGenerator = new FakeUUIDGenerator(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

    /**
     * objects
     */
    private Product product;
    private ProductPost productPost;
    private PricingPlan pricingPlan;
    private ProductPostSnapshot productPostSnapshot;
    private ProductLink productLink;
    private Tag tag;
    private Long productId = 1L;
    private Long productPostId = 1L;
    private String imageId = String.valueOf(uuidGenerator.generateV7());
    private Long memberId = 1L;
    private Long productPostSnapshotId = 1L;
    private Long planId = 1L;
    private Long linkId = 1L;
    private Long tagId = 1L;
    private String productName = "chatGpt";
    private String plan = "FREE";
    private String productUrl = "https://openai.com";
    private String tagName = "ai";

    @BeforeEach
    void setUp() {

        productPostService = new ProductPostService(
            productRepository,
            postRepository,
            planService,
            postLastSnapshotRepository,
            linkRepository,
            snapshotAttachmentFileRepository,
            makerRepository,
            tagService,
            postSnapshotTagRepository,
            postSnapshotRepository,
            timeHolder,
            uuidGenerator
        );

        product = Product.builder()
            .id(productId)
            .name("chatGpt")
            .cratedAt(timeHolder.now())
            .build();

        productPost = ProductPost.builder()
            .id(productPostId)
            .writerId(memberId)
            .productId(productId)
            .createdAt(timeHolder.now())
            .recommendCount(0L)
            .build();

        pricingPlan = PricingPlan.builder()
            .id(planId)
            .name("FREE")
            .build();

        productPostSnapshot = ProductPostSnapshot.builder()
            .id(productPostSnapshotId)
            .postId(productPostId)
            .pricingPlanId(planId)
            .title("chatGpt")
            .description("chatGpt is good")
            .createdAt(timeHolder.now())
            .build();

        productLink = ProductLink.builder()
            .id(linkId)
            .productId(productId)
            .url(productUrl)
            .createdAt(timeHolder.now())
            .build();

        tag = Tag.builder()
            .id(tagId)
            .name(tagName)
            .build();
    }

    @DisplayName("성공[create] - 제품이 존재하지 않아 새제품 생성 후 게시글 생성")
    @Test
    void create_success_newProduct() {

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name("newProduct")
            .url(productUrl)
            .description("new product description")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("newTag"))
            .pricingPlan(plan)
            .build();


        Product newProduct = Product.builder()
            .id(2L)
            .name("newProduct")
            .cratedAt(timeHolder.now())
            .build();

        // 새로운 제품이니 empty 리턴 -> DB저장
        when(productRepository.findByName("newProduct")).thenReturn(Optional.empty());
        when(productRepository.save("newProduct", timeHolder.now())).thenReturn(newProduct);
        when(postRepository.save(memberId, 2L, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(productPostSnapshot);
        when(postLastSnapshotRepository.save(anyLong(), anyLong())).thenReturn(null);
        when(linkRepository.findByUrl(request.url())).thenReturn(Optional.ofNullable(productLink));
        when(tagService.getOrCreateTag("newTag")).thenReturn(tag);

        // when
        Long result = productPostService.create(request, memberId);

        // then
        assertThat(result).isEqualTo(1L);
    }


    @DisplayName("성공[create] - 기존 제품 이용하여 게시글 생성")
    @Test
    void create_success() {

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name("chatGpt")
            .url(productUrl)
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("ai"))
            .pricingPlan(plan)
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.ofNullable(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(null);
        when(linkRepository.findByUrl(request.url())).thenReturn(Optional.ofNullable(productLink));
        when(tagService.getOrCreateTag(tagName)).thenReturn(tag);

        Long result = productPostService.create(request, memberId);
        assertThat(result).isEqualTo(1L);
    }


}