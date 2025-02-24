package net.detalk.api.post.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidRecommendCountRequest extends ApiException {

    public InvalidRecommendCountRequest(int count) {
        super(String.format("추천 수는 양수여야 합니다. count=%d", count));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "invalid_recommend_count";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }

}
