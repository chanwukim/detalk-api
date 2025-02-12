package net.detalk.api.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.detalk.api.controller.v1.request.CreateRecommendRequest;
import net.detalk.api.domain.Recommend;
import net.detalk.api.domain.exception.DuplicateRecommendationException;
import net.detalk.api.domain.exception.ProductPostNotFoundException;
import net.detalk.api.mock.FakeTimeHolder;
import net.detalk.api.repository.RecommendProductRepository;
import net.detalk.api.repository.RecommendRepository;
import net.detalk.api.support.TimeHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock
    private RecommendRepository recommendRepository;
    @Mock
    private RecommendProductRepository recommendProductRepository;
    @Mock
    private ProductPostService productPostService;

    private TimeHolder timeHolder;

    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        timeHolder = new FakeTimeHolder(Instant.parse("2025-01-01T12:00:00Z"));
        recommendService = new RecommendService(
            recommendRepository,
            recommendProductRepository,
            productPostService,
            timeHolder
        );
    }

    @DisplayName("[addRecommendation] 게시글 추천 시, 연관관계와 추천수가 저장되어야한다")
    @Test
    void addRecommendation_WhenAllValid_ShouldSaveAndIncrement() {

        // given
        var postId = 1L;
        var memberId = 1L;
        var recommendId = 100L;
        var reason1 = "좋음";
        var reason2 = "저렴함";
        var content = "디자인이 이쁘고 저렴해요.";
        var createRequest = new CreateRecommendRequest(List.of(reason1, reason2), content);

        var existsRecommend1 = Recommend.builder()
            .id(recommendId)
            .value(reason1)
            .build();

        var existsRecommend2 = Recommend.builder()
            .id(recommendId)
            .value(reason2)
            .build();

        when(recommendRepository.findByReason(reason1))
            .thenReturn(Optional.of(existsRecommend1));

        when(recommendRepository.findByReason(reason2))
            .thenReturn(Optional.of(existsRecommend2));

        when(recommendProductRepository.isAlreadyRecommended(memberId, recommendId, postId))
            .thenReturn(false);

        // when
        recommendService.addRecommendation(postId, memberId, createRequest);

        // then 연관 관계 호출되었는지
        verify(recommendProductRepository).saveAll(argThat(
            list -> list.size() == 2
                && list.get(0).getRecommendId().equals(recommendId)
                && list.get(0).getMemberId().equals(memberId)
                && list.get(0).getProductPostId().equals(postId)
                && list.get(0).getContent().equals(content)
        ));

        // 추천수가 1증가 했는지
        verify(productPostService).incrementRecommendCount(postId, 2);
    }

    @DisplayName("[addRecommendation] 게시글 추천 시, 연관관계 저장 전, 중복 추천이면 예외 발생")
    @Test
    void addRecommendation_WhenDuplicateRecommendation_ShouldThrowException() {

        // given
        var postId = 1L;
        var memberId = 1L;
        var recommendId = 100L;
        var reason = "중복추천";
        var content = "중복 추천 테스트";
        var createRequest = new CreateRecommendRequest(List.of(reason),content);

        // 이미 해당 추천 이유가 존재한다고 가정
        var existsRecommend = Recommend.builder()
            .id(recommendId)
            .value(reason)
            .build();
        when(recommendRepository.findByReason(reason))
            .thenReturn(Optional.of(existsRecommend));

        // 중복 추천인 경우 isAlreadyRecommended가 true를 반환
        when(recommendProductRepository.isAlreadyRecommended(memberId, recommendId, postId))
            .thenReturn(true);

        // when & then
        assertThatThrownBy(
            () -> recommendService.addRecommendation(postId, memberId, createRequest))
            .isInstanceOf(DuplicateRecommendationException.class)
            .hasMessageContaining(reason);

        // 중복 추천 예외 발생 시, 이후 로직(추천 기록 저장, 추천수 증가)이 호출되지 않아야 함
        verify(recommendProductRepository, never()).saveAll(any());
        verify(productPostService, never()).incrementRecommendCount(anyLong(), anyInt());
    }

    @DisplayName("[addRecommendation] 게시글 추천 시, 존재하지 않는 게시글이면 예외 발생")
    @Test
    void addRecommendation_WhenPostDoesNotExist_ShouldThrowException() {

        // given
        var postId = 999L; // 없는 게시글 ID
        var memberId = 1L;
        var reason = "좋음";
        var content = "존재하지 않는 게시글 테스트";
        var createRequest = new CreateRecommendRequest(List.of(reason),content);

        // 게시글 존재 여부 검증에서 예외를 발생하도록 설정
        doThrow(new ProductPostNotFoundException(postId))
            .when(productPostService).validatePostExists(postId);

        // when & then
        assertThatThrownBy(
            () -> recommendService.addRecommendation(postId, memberId, createRequest))
            .isInstanceOf(ProductPostNotFoundException.class)
            .hasMessageContaining(String.format("상품-게시글(ID: %d)을 찾을 수 없습니다.", postId));

        // 게시글이 없으므로, 이후 Repository 호출은 이뤄지지 않아야 함
        verifyNoInteractions(recommendRepository);
        verifyNoInteractions(recommendProductRepository);
    }
}