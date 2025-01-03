package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.detalk.api.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse.Media;
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
import net.detalk.api.support.CursorPageData;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.UUIDGenerator;
import net.detalk.api.support.error.ApiException;
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
    private String productName;
    private String plan;
    private String productUrl;
    private String tagName;
    private String nickname;
    private String userhandle;
    private String avatarUrl = String.valueOf(uuidGenerator.generateV7());
    private List<Media> mediaList;

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

        productName = "chatGpt";
        plan = "FREE";
        productUrl = "https://openai.com";
        tagName = "ai";
        nickname = "foo";
        userhandle = "foo_handle";

        tag = Tag.builder()
            .id(tagId)
            .name(tagName)
            .build();

        mediaList = List.of(
            new Media("https://image1.com", 1),
            new Media("https://image2.com", 2)
        );
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

    @DisplayName("성공[create] - 새 링크 저장 후, 게시글 생성")
    @Test
    void create_success_newLink() {

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name(productName)
            .url("https://newlink.com")
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("ai"))
            .pricingPlan(plan)
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(null);
        when(linkRepository.findByUrl("https://newlink.com")).thenReturn(Optional.empty());
        when(linkRepository.save(productId, "https://newlink.com", timeHolder.now())).thenReturn(productLink);
        when(tagService.getOrCreateTag(tagName)).thenReturn(tag);

        // when
        Long result = productPostService.create(request, memberId);

        // then
        assertThat(result).isEqualTo(1L);
    }


    @DisplayName("성공[create] - 새로운 태그가 생성됨")
    @Test
    void create_success_newTag() {
        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name(productName)
            .url(productUrl)
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("newTag"))
            .pricingPlan(plan)
            .build();

        Tag newTag = Tag.builder()
            .id(2L)
            .name("newTag")
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(null);
        when(linkRepository.findByUrl(productUrl)).thenReturn(Optional.of(productLink));
        when(tagService.getOrCreateTag("newTag")).thenReturn(newTag);

        // when
        Long result = productPostService.create(request, memberId);

        // then
        assertThat(result).isEqualTo(1L);
    }


    @DisplayName("성공[getProductPosts] - 다음 데이터가 없으면 hasNext,nextId null 을 반환한다")
    @Test
    void getProductPosts_success_lessThanPageSize() {

        // given
        int pageSize = 5;
        Long nextId = null;

        GetProductPostResponse postResponse = GetProductPostResponse.builder()
            .id(productId)
            .nickname(nickname)
            .userHandle(userhandle)
            .createdAt(timeHolder.now())
            .isMaker(true)
            .avatarUrl(avatarUrl)
            .title(productPostSnapshot.getTitle())
            .description(productPostSnapshot.getDescription())
            .pricingPlan(pricingPlan.getName())
            .recommendCount(0)
            .tags(List.of(String.valueOf(tag)))
            .media(mediaList)
            .urls(List.of(productUrl))
            .build();

        List<GetProductPostResponse> mockPosts = List.of(postResponse);

        when(postRepository.findProductPosts(pageSize + 1, nextId)).thenReturn(mockPosts);

        CursorPageData<GetProductPostResponse> result = productPostService.getProductPosts(pageSize, nextId);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getNextId()).isNull();
        assertThat(result.hasNext()).isFalse();
    }

    @DisplayName("성공[getProductPosts] - pageSize=5, 데이터가 6개일 때 다음 데이터가 있어야 한다")
    @Test
    void getProductPosts_success() {

        // given
        int pageSize = 5;
        Long nextId = null;
        int numOfData = 6; // pageSize + 1
        long startId = 0L; // 시작 ID

        List<GetProductPostResponse> responses = new ArrayList<>();

        for (int i = 1; i <= numOfData; i++) {
            long currentId = startId + i;
            GetProductPostResponse response = GetProductPostResponse.builder()
                .id(currentId)
                .nickname("nickname" + currentId)
                .userHandle("userHandle" + currentId)
                .createdAt(timeHolder.now())
                .isMaker(currentId % 2 == 0)
                .avatarUrl("https://avatar.url/" + currentId)
                .title("Title " + currentId)
                .description("Description " + currentId)
                .pricingPlan(pricingPlan.getName())
                .recommendCount(0)
                .tags(List.of("tag" + currentId))
                .media(mediaList)
                .urls(List.of(productUrl))
                .build();
            responses.add(response);
        }

        when(postRepository.findProductPosts(pageSize + 1, nextId)).thenReturn(responses);

        // When
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPosts(pageSize, nextId);

        // Then
        assertThat(result.getItems()).hasSize(pageSize); // 5개만 반환
        assertThat(result.getNextId()).isEqualTo(5L); // 5번째 게시글의 ID
        assertThat(result.hasNext()).isTrue(); // 다음 페이지 존재


    }

    @DisplayName("실패[getProductPosts] - 잘못된 pageSize")
    @Test
    void getProductPosts_fail_invalidPageSize() {

        // given
        int pageSizeZero = 0;
        int pageSizeNegative = -99;
        Long nextId = null;

        // when
        ApiException exceptionZero = assertThrows(ApiException.class,
            () -> productPostService.getProductPosts(pageSizeZero, nextId));
        ApiException exceptionNegative = assertThrows(ApiException.class,
            () -> productPostService.getProductPosts(pageSizeNegative, nextId));

        // then
        assertThat(exceptionZero.getMessage()).isEqualTo("Bad Request.");
        assertThat(exceptionNegative.getMessage()).isEqualTo("Bad Request.");

    }


    @DisplayName("성공[getProductPostsByMemberId] - pageSize=5, 데이터가 6개일 때(hasNext=true)")
    @Test
    void getProductPostsByMemberId_success_hasNext() {
        // Given
        Long memberId = 1L;
        int pageSize = 5;
        Long nextId = null;
        int numOfData = 6;

        List<GetProductPostResponse> responses = new ArrayList<>();

        for (int i = 1; i <= numOfData; i++) {
            long currentId = i;
            GetProductPostResponse response = GetProductPostResponse.builder()
                .id(currentId)
                .nickname("nickname" + currentId)
                .userHandle("userHandle" + currentId)
                .createdAt(timeHolder.now())
                .isMaker(currentId % 2 == 0)
                .avatarUrl("https://avatar.url/" + currentId)
                .title("Title " + currentId)
                .description("Description " + currentId)
                .pricingPlan(pricingPlan.getName())
                .recommendCount(0)
                .tags(List.of("tag" + currentId))
                .media(mediaList)
                .urls(List.of(productUrl))
                .build();
            responses.add(response);
        }

        when(postRepository.findProductPostsByMemberId(memberId, pageSize + 1, nextId)).thenReturn(responses);

        // When
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPostsByMemberId(memberId, pageSize, nextId);

        // Then
        assertThat(result.getItems()).hasSize(pageSize);
        assertThat(result.getNextId()).isEqualTo(5L);
        assertThat(result.hasNext()).isTrue();
    }


    @DisplayName("실패[getProductPostsByMemberId] - 유효하지 않은 pageSize 입력 시 예외 발생")
    @Test
    void getProductPostsByMemberId_fail_invalidPageSize() {

        // given
        Long memberId = 1L;
        int pageSize = 0;
        Long nextId = null;

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.getProductPostsByMemberId(memberId, pageSize, nextId));

        // then
        assertThat(exception.getMessage()).isEqualTo("Bad Request.");
    }


}