package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import net.detalk.api.controller.v1.request.UpdateProductPostRequest;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse.Media;
import net.detalk.api.domain.PricingPlan;
import net.detalk.api.domain.Product;
import net.detalk.api.domain.ProductLink;
import net.detalk.api.domain.ProductPost;
import net.detalk.api.domain.ProductPostSnapshot;
import net.detalk.api.domain.Tag;
import net.detalk.api.domain.exception.InvalidPageSizeException;
import net.detalk.api.domain.exception.InvalidRecommendCountRequest;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.mock.FakeUUIDGenerator;
import net.detalk.api.repository.ProductLinkRepository;
import net.detalk.api.repository.ProductMakerRepository;
import net.detalk.api.repository.ProductPostLastSnapshotRepository;
import net.detalk.api.repository.ProductPostLinkRepository;
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
    private ProductPostLinkRepository productPostLinkRepository;
    @Mock
    private TagService tagService;
    @Mock
    private PricingPlanService planService;
    @Mock
    private ProductPostIdempotentService postIdempotentService;

    /**
     * fake random classes
     */
    private TimeHolder timeHolder = new FakeTimeHolder(Instant.parse("2025-01-01T12:00:00Z"));
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
            productPostLinkRepository,
            timeHolder,
            uuidGenerator,
            postIdempotentService
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

        UUID idempotentKey = uuidGenerator.generateV7();

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name("newProduct")
            .url(productUrl)
            .description("new product description")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("newTag"))
            .pricingPlan(plan)
            .idempotentKey(String.valueOf(idempotentKey))
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
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(
            productPostSnapshot);
        when(postLastSnapshotRepository.save(anyLong(), anyLong())).thenReturn(null);
        when(linkRepository.findByUrl(request.url())).thenReturn(Optional.ofNullable(productLink));
        when(tagService.getOrCreateTag("newTag")).thenReturn(tag);
        when(postIdempotentService.insertIdempotentKey(idempotentKey, timeHolder.now())).thenReturn(
            true);

        // when
        Long result = productPostService.create(request, memberId);

        // then
        assertThat(result).isEqualTo(1L);
    }


    @DisplayName("성공[create] - 기존 제품 이용하여 게시글 생성")
    @Test
    void create_success() {
        UUID idempotentKey = uuidGenerator.generateV7();
        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name("chatGpt")
            .url(productUrl)
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("ai"))
            .pricingPlan(plan)
            .idempotentKey(String.valueOf(idempotentKey))
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.ofNullable(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(
            productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(
            null);
        when(linkRepository.findByUrl(request.url())).thenReturn(Optional.ofNullable(productLink));
        when(tagService.getOrCreateTag(tagName)).thenReturn(tag);
        when(postIdempotentService.insertIdempotentKey(idempotentKey, timeHolder.now())).thenReturn(
            true);

        Long result = productPostService.create(request, memberId);
        assertThat(result).isEqualTo(1L);
    }

    @DisplayName("성공[create] - 새 링크 저장 후, 게시글 생성")
    @Test
    void create_success_newLink() {

        UUID idempotentKey = uuidGenerator.generateV7();

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name(productName)
            .url("https://newlink.com")
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("ai"))
            .pricingPlan(plan)
            .idempotentKey(String.valueOf(idempotentKey))
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(
            productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(
            null);
        when(linkRepository.findByUrl("https://newlink.com")).thenReturn(Optional.empty());
        when(linkRepository.save(productId, "https://newlink.com", timeHolder.now())).thenReturn(
            productLink);
        when(tagService.getOrCreateTag(tagName)).thenReturn(tag);

        when(postIdempotentService.insertIdempotentKey(idempotentKey, timeHolder.now())).thenReturn(
            true);

        // when
        Long result = productPostService.create(request, memberId);

        // then
        assertThat(result).isEqualTo(1L);
    }


    @DisplayName("성공[create] - 새로운 태그가 생성됨")
    @Test
    void create_success_newTag() {

        UUID idempotentKey = uuidGenerator.generateV7();

        // given
        CreateProductPostRequest request = CreateProductPostRequest.builder()
            .name(productName)
            .url(productUrl)
            .description("ai skills")
            .imageIds(List.of(imageId))
            .isMaker(false)
            .tags(List.of("newTag"))
            .pricingPlan(plan)
            .idempotentKey(String.valueOf(idempotentKey))
            .build();

        Tag newTag = Tag.builder()
            .id(2L)
            .name("newTag")
            .build();

        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(postRepository.save(memberId, productId, timeHolder.now())).thenReturn(productPost);
        when(planService.findByName(plan)).thenReturn(pricingPlan);
        when(postSnapshotRepository.save(any(ProductPostSnapshot.class))).thenReturn(
            productPostSnapshot);
        when(postLastSnapshotRepository.save(productPostId, productPostSnapshotId)).thenReturn(
            null);
        when(linkRepository.findByUrl(productUrl)).thenReturn(Optional.of(productLink));
        when(tagService.getOrCreateTag("newTag")).thenReturn(newTag);

        when(postIdempotentService.insertIdempotentKey(idempotentKey, timeHolder.now())).thenReturn(
            true);

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

        when(postRepository.findProductPostsByMemberId(memberId, pageSize + 1, nextId)).thenReturn(
            responses);

        // When
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPostsByMemberId(
            memberId, pageSize, nextId);

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

    @DisplayName("성공[update]")
    @Test
    void update_success() {
        String newImgId = String.valueOf(uuidGenerator.generateV7());
        String newPlanName = "PAY";
        Long newPlanId = 2L;
        Long newSnapshotId = 2L;
        String newDescription = "new description";

        String newTag1 = "newTag1";
        String newTag2 = "newTag2";
        List<String> newTags = List.of(newTag1, newTag2);
        String newUrl = "newUrl";

        PricingPlan newPlan = PricingPlan.builder()
            .id(newPlanId)
            .name(newPlanName)
            .build();

        UpdateProductPostRequest updateRequest = UpdateProductPostRequest.builder()
            .name(productPostSnapshot.getTitle())
            .pricingPlan(newPlanName)
            .description(newDescription)
            .tags(newTags)
            .url(newUrl)
            .imageIds(List.of(newImgId))
            .isMaker(true)
            .build();

        when(postRepository.findById(productPostId)).thenReturn(Optional.ofNullable(productPost));
        when(productRepository.findByName(productName)).thenReturn(Optional.ofNullable(product));
        when(planService.findByName(newPlanName)).thenReturn(newPlan);

        ProductPostSnapshot newSnapshot = ProductPostSnapshot.builder()
            .id(newSnapshotId)
            .postId(productPostId)
            .pricingPlanId(newPlanId)
            .title(productPostSnapshot.getTitle())
            .description(newDescription)
            .createdAt(timeHolder.now())
            .build();

        when(postSnapshotRepository.save(any())).thenReturn(newSnapshot);

        when(tagService.getOrCreateTag("newTag1")).thenReturn(
            Tag.builder()
                .id(1L)
                .name(newTag1)
                .build()
        );

        when(tagService.getOrCreateTag("newTag2")).thenReturn(
            Tag.builder()
                .id(2L)
                .name(newTag2)
                .build()
        );

        when(postLastSnapshotRepository.update(productPostId, newSnapshot)).thenReturn(1);

        // when
        Long result = productPostService.update(productPostId, updateRequest, memberId);

        // then
        assertThat(result).isEqualTo(newSnapshotId);
    }

    @DisplayName("실패[update] - 게시글이 존재하지 않음")
    @Test
    void update_fail_NotExistsPost() {

        String newImgId = String.valueOf(uuidGenerator.generateV7());
        String newPlanName = "PAY";
        String newDescription = "new description";

        String newTag1 = "newTag1";
        String newTag2 = "newTag2";
        List<String> newTags = List.of(newTag1, newTag2);
        String newUrl = "newUrl";

        Long notExistsId = 9999L;

        UpdateProductPostRequest updateRequest = UpdateProductPostRequest.builder()
            .name(productPostSnapshot.getTitle())
            .pricingPlan(newPlanName)
            .description(newDescription)
            .tags(newTags)
            .url(newUrl)
            .imageIds(List.of(newImgId))
            .isMaker(true)
            .build();

        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.update(notExistsId, updateRequest, memberId));

        assertThat(exception.getMessage()).isEqualTo("상품-게시글(ID: 9999)을 찾을 수 없습니다.");
    }

    @DisplayName("실패[update] - 최근 스냅샷 업데이트 실패")
    @Test
    void update_fail_LastSnapShotUpdate() {

        String newImgId = String.valueOf(uuidGenerator.generateV7());
        String newPlanName = "PAY";
        Long newPlanId = 2L;
        Long newSnapshotId = 2L;
        String newDescription = "new description";

        String newTag1 = "newTag1";
        String newTag2 = "newTag2";
        List<String> newTags = List.of(newTag1, newTag2);
        String newUrl = "newUrl";

        PricingPlan newPlan = PricingPlan.builder()
            .id(newPlanId)
            .name(newPlanName)
            .build();

        UpdateProductPostRequest updateRequest = UpdateProductPostRequest.builder()
            .name(productPostSnapshot.getTitle())
            .pricingPlan(newPlanName)
            .description(newDescription)
            .tags(newTags)
            .url(newUrl)
            .imageIds(List.of(newImgId))
            .isMaker(true)
            .build();

        when(postRepository.findById(productPostId)).thenReturn(Optional.ofNullable(productPost));
        when(productRepository.findByName(productName)).thenReturn(Optional.ofNullable(product));
        when(planService.findByName(newPlanName)).thenReturn(newPlan);

        ProductPostSnapshot newSnapshot = ProductPostSnapshot.builder()
            .id(newSnapshotId)
            .postId(productPostId)
            .pricingPlanId(newPlanId)
            .title(productPostSnapshot.getTitle())
            .description(newDescription)
            .createdAt(timeHolder.now())
            .build();

        when(postSnapshotRepository.save(any())).thenReturn(newSnapshot);

        when(tagService.getOrCreateTag("newTag1")).thenReturn(
            Tag.builder()
                .id(1L)
                .name(newTag1)
                .build()
        );

        when(tagService.getOrCreateTag("newTag2")).thenReturn(
            Tag.builder()
                .id(2L)
                .name(newTag2)
                .build()
        );

        ApiException exception = assertThrows(ApiException.class,
            () -> productPostService.update(productPostId, updateRequest, memberId));

        assertThat(exception.getMessage()).isEqualTo("스냅샷 업데이트에 실패했습니다. postId=1, newPostSnapshot=2");

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
        var productPostId = 1L;
        int recommendCounts = 999;

        productPostService.incrementRecommendCount(productPostId, recommendCounts);
    }

    @DisplayName("[incrementRecommendCount] 음수 추천 수 입력 시 예외가 발생해야 한다")
    @Test
    void incrementRecommendCount_throwsException_whenCountIsNegative() {
        var productPostId = 1L;
        int recommendCounts = -999;

        InvalidRecommendCountRequest exception = assertThrows(InvalidRecommendCountRequest.class,
            () -> productPostService.incrementRecommendCount(productPostId, recommendCounts));

        assertThat(exception.getMessage()).isEqualTo("추천 수는 양수여야 합니다. count=-999");
        assertThat(exception.getErrorCode()).isEqualTo("invalid_recommend_count");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);


    }
}