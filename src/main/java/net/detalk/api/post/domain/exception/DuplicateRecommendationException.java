package net.detalk.api.post.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateRecommendationException extends ApiException {

    public DuplicateRecommendationException(Long memberId, Long recommendId, Long postId,
        String reason) {
        super(String.format("중복 추천 시도: 회원 ID=%d, 게시글 ID=%d, 추천 이유 ID=%d, 추천 이유=%s",
            memberId, postId, recommendId, reason));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "duplicate_recommendation";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}