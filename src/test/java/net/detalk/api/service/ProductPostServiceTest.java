package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.detalk.api.link.domain.ShortLink;
import net.detalk.api.link.service.ShortLinkService;
import net.detalk.api.post.controller.v1.request.CreateProductPostRequest;
import net.detalk.api.post.controller.v1.response.CreateProductPostResponse;
import net.detalk.api.post.controller.v1.response.GetProductPostResponse;
import net.detalk.api.post.controller.v1.response.GetProductPostResponse.Media;
import net.detalk.api.plan.domain.PricingPlan;
import net.detalk.api.plan.service.PricingPlanService;
import net.detalk.api.post.domain.exception.InvalidRecommendCountRequest;
import net.detalk.api.post.repository.ProductPostLastSnapshotRepository;
import net.detalk.api.post.repository.ProductPostLinkRepository;
import net.detalk.api.post.repository.ProductPostRepository;
import net.detalk.api.post.repository.ProductPostSnapshotAttachmentFileRepository;
import net.detalk.api.post.repository.ProductPostSnapshotRepository;
import net.detalk.api.post.repository.ProductPostSnapshotTagRepository;
import net.detalk.api.product.domain.Product;
import net.detalk.api.product.domain.ProductLink;
import net.detalk.api.post.domain.ProductPost;
import net.detalk.api.post.domain.ProductPostSnapshot;
import net.detalk.api.product.service.ProductLinkService;
import net.detalk.api.product.service.ProductService;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.paging.CursorPageData;
import net.detalk.api.tag.domain.Tag;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import net.detalk.api.product.repository.ProductMakerRepository;
import net.detalk.api.post.service.ProductPostIdempotentService;
import net.detalk.api.post.service.ProductPostService;
import net.detalk.api.support.util.TimeHolder;
import net.detalk.api.support.util.UUIDGenerator;
import net.detalk.api.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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
    private ProductLinkService productLinkService;
    @Mock
    private ProductPostRepository postRepository;
    @Mock
    private ProductPostLastSnapshotRepository postLastSnapshotRepository;
    @Mock
    private ProductPostSnapshotAttachmentFileRepository snapshotAttachmentFileRepository;
    @Mock
    private ProductMakerRepository makerRepository;
    @Mock
    private ProductPostSnapshotTagRepository postSnapshotTagRepository;
    @Mock
    private ProductPostSnapshotRepository postSnapshotRepository;
    @Mock
    private ProductPostLinkRepository productPostLinkRepository;
    @Mock
    private ProductService productService;
    @Mock
    private TagService tagService;
    @Mock
    private PricingPlanService planService;
    @Mock
    private ProductPostIdempotentService idempotentService;
    @Mock
    private ShortLinkService shortLinkService;

    /**
     * fake random classes
     */
    private final LocalDateTime fixedLocalDateTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    private final Instant fixedInstant = Instant.parse("2025-01-01T12:00:00Z");

    private TimeHolder timeHolder = new FakeTimeHolder(fixedInstant, fixedLocalDateTime);
    private UUIDGenerator uuidGenerator = new FakeUUIDGenerator(
        UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

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
    private String description;
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
            productLinkService,
            makerRepository,
            productService,
            postRepository,
            postLastSnapshotRepository,
            snapshotAttachmentFileRepository,
            postSnapshotTagRepository,
            postSnapshotRepository,
            productPostLinkRepository,
            idempotentService,
            shortLinkService,
            planService,
            tagService,
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
        description = "description";
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

    @DisplayName("성공[create] - 게시글 생성")
    @Test
    void create_ProductAndPost_success_newProduct() {
        // given
        UUID idempotentKey = uuidGenerator.generateV7();
        String shortCode = "shortCode";

        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name(productName)
            .url(productUrl)
            .description(description)
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of(tagName))
            .pricingPlan(plan)
            .idempotentKey(String.valueOf(idempotentKey))
            .build();

        ShortLink shortLink = ShortLink.builder()
            .id(1L)
            .createdAt(fixedInstant)
            .creatorId(memberId)
            .originalUrl(productUrl)
            .shortCode(shortCode)
            .build();

        CreateProductPostResponse responseBody = new CreateProductPostResponse(
            productPostId,
            shortLink.getShortCode()
        );

        when(idempotentService.insertIdempotentKey(request.idempotentKey(),
            timeHolder.now())).thenReturn(true);
        when(productService.getOrCreateProduct(productName, fixedInstant)).thenReturn(product);
        when(postRepository.save(memberId, productPostId, fixedInstant)).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(
            productPostSnapshot);
        when(postLastSnapshotRepository.save(anyLong(), anyLong())).thenReturn(null);
        when(productLinkService.getOrCreateProductLink(productUrl, productId,
            fixedInstant)).thenReturn(productLink);
        when(tagService.getOrCreateTag(tagName)).thenReturn(tag);
        when(shortLinkService.createShortLink(productUrl, memberId)).thenReturn(shortLink);


        CreateProductPostResponse result = productPostService.createProductAndPost(request,
            memberId);

        // then
        assertThat(result).isEqualTo(responseBody);
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.shortLink()).isEqualTo(shortCode);
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

        CursorPageData<GetProductPostResponse> result = productPostService.getProductPosts(pageSize,
            nextId);

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
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPosts(pageSize,
            nextId);

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
        assertThat(exceptionZero.getMessage()).isEqualTo("잘못된 페이지 크기입니다: 0 (허용 범위: 1-20)");
        assertThat(exceptionNegative.getMessage()).isEqualTo("잘못된 페이지 크기입니다: -99 (허용 범위: 1-20)");

    }

    @DisplayName("성공[getProductPostsByMemberId] - pageSize=5, 데이터가 6개일 때(hasNext=true)")
    @Test
    void getProductPostsByMemberId_success_hasNext() {

        // given
        Long testMemberId = 1L;
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

        when(postRepository.findProductPostsByMemberId(testMemberId, pageSize + 1, nextId)).thenReturn(
            responses);

        // When
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPostsByMemberId(
            testMemberId, pageSize, nextId);

        // Then
        assertThat(result.getItems()).hasSize(pageSize);
        assertThat(result.getNextId()).isEqualTo(5L);
        assertThat(result.hasNext()).isTrue();
    }


    @DisplayName("실패[getProductPostsByMemberId] - 유효하지 않은 pageSize 입력 시 예외 발생")
    @Test
    void getProductPostsByMemberId_fail_invalidPageSize() {

        // given
        Long testMemberId = 1L;
        int pageSize = 0;
        Long nextId = null;

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.getProductPostsByMemberId(testMemberId, pageSize, nextId));

        // then
        assertThat(exception.getMessage()).isEqualTo("잘못된 페이지 크기입니다: 0 (허용 범위: 1-20)");
    }


    @DisplayName("성공[getProductPostDetailsById]")
    @Test
    void getProductPostDetailsById() {

        // given
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
        Long id = 1L;

        when(postRepository.findDetailsById(id)).thenReturn(Optional.ofNullable(postResponse));

        // when
        GetProductPostResponse result = productPostService.getProductPostDetailsById(
            id);

        // then
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.userHandle()).isEqualTo(userhandle);
        assertThat(result.createdAt()).isEqualTo(timeHolder.now());
        assertThat(result.avatarUrl()).isEqualTo(avatarUrl);
        assertThat(result.description()).isEqualTo(productPostSnapshot.getDescription());
        assertThat(result.pricingPlan()).isEqualTo(plan);
        assertThat(result.tags()).isEqualTo(List.of(String.valueOf(tag)));
        assertThat(result.media()).isEqualTo(mediaList);
        assertThat(result.urls()).isEqualTo(List.of(productUrl));
    }

    @DisplayName("실패[getProductPostDetailsById] - 존재하지 않는 게시글 ID")
    @Test
    void getProductPostDetailsById_Fail_NotExistsId() {

        // given
        Long id = 9999L;

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.getProductPostDetailsById(id));

        // then
        assertThat(exception.getMessage()).isEqualTo("상품-게시글(ID: 9999)을 찾을 수 없습니다.");
    }




    @DisplayName("성공[getRecommendedPostsByMemberId]")
    @Test
    void getRecommendedPostsByMemberId_success() {

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

        List<GetProductPostResponse> mockResponses = List.of(postResponse);

        when(postRepository.findRecommendedPostsByMemberId(memberId, pageSize + 1,
            nextId)).thenReturn(mockResponses);

        // when
        CursorPageData<GetProductPostResponse> recommendedPostsByMemberId = productPostService.getRecommendedPostsByMemberId(
            memberId, pageSize, nextId);

        // then
        assertThat(recommendedPostsByMemberId.getNextId()).isNull();
    }

    @DisplayName("성공[validatePostExists] - 게시글이 존재할경우 아무것도 안한다")
    @Test
    void validatePostExists_WhenPostExists_ShouldDoNothing() {

        // given
        Long existsId = 1L;
        when(postRepository.existsById(existsId)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> productPostService.validatePostExists(existsId));
    }


    @DisplayName("성공[validatePostExists] - 게시글이 존재할경우 예외가 발생한다")
    @Test
    void validatePostExists_WhenPostDoesNotExist_ShouldThrowApiException() {

        // given
        Long nonExistingId = 1L;
        when(postRepository.existsById(nonExistingId)).thenReturn(false);

        // when
        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.validatePostExists(nonExistingId));

        assertThat(exception.getMessage()).isEqualTo("상품-게시글(ID: 1)을 찾을 수 없습니다.");
    }

    @DisplayName("[incrementRecommendCount] 성공적으로 counts 만큼 추천 되어야 한다")
    @Test
    void incrementRecommendCount() {

        // given
        var testPostId = 1L;
        int recommendCounts = 999;

        // when
        productPostService.incrementRecommendCount(testPostId, recommendCounts);

        // then
        verify(postRepository).incrementRecommendCount(testPostId, recommendCounts);

    }

    @DisplayName("[incrementRecommendCount] 음수 추천 수 입력 시 예외가 발생해야 한다")
    @Test
    void incrementRecommendCount_throwsException_whenCountIsNegative() {
        var testPostId = 1L;
        int recommendCounts = -999;

        InvalidRecommendCountRequest exception = assertThrows(InvalidRecommendCountRequest.class,
            () -> productPostService.incrementRecommendCount(testPostId, recommendCounts));

        assertThat(exception.getMessage()).isEqualTo("추천 수는 양수여야 합니다. count=-999");
        assertThat(exception.getErrorCode()).isEqualTo("invalid_recommend_count");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);


    }
}