package net.detalk.api.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.domain.CreateRecommend;
import net.detalk.api.domain.Recommend;
import net.detalk.api.repository.ProductPostRepository;
import net.detalk.api.repository.RecommendProductRepository;
import net.detalk.api.repository.RecommendRepository;
import net.detalk.api.support.TimeHolder;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService {

    private final RecommendRepository recommendRepository;
    private final RecommendProductRepository recommendProductRepository;
    private final ProductPostRepository productPostRepository;
    private final TimeHolder timeHolder;

    @Transactional
    public void addRecommendation(Long postId, CreateRecommend createRecommend) {

        Instant now = timeHolder.now();

        // 추천 하려는 게시글이 존재하는지 검증
        if (!productPostRepository.existsById(postId)) {
            log.error("[addRecommendation] 게시글이 존재하지 않습니다 : {}" , postId);
            throw new ApiException(ErrorCode.NOT_FOUND);
        }

        // 추천 이유 존재하면 그대로 사용, 없으면 DB 저장
        String reason = createRecommend.getReason();
        Long recommendId = recommendRepository.findByReason(reason)
            .map(Recommend::getId)
            .orElseGet(() -> recommendRepository.save(reason, now).getId());

        // 중복 추천 예외
        boolean alreadyRecommended = recommendProductRepository.isAlreadyRecommended(
            createRecommend.getMemberId(), recommendId,
            postId);

        if (alreadyRecommended) {
            log.warn("[addRecommendation] 중복 추천 시도 : 회원 ID={}, 게시글 ID={}, 추천 이유 ID={}, 추천 이유 ={}",
                createRecommend.getMemberId(), postId, recommendId, createRecommend.getMemberId());
            throw new ApiException(ErrorCode.CONFLICT);
        }

        // 게시글 추천, 게시글 연관관계 맺기
        recommendProductRepository.save(recommendId, postId, createRecommend.getMemberId(), now);

        // 추천수 증가
        productPostRepository.incrementRecommendCount(postId);
    }

}
