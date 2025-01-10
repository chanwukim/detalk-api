package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ProductPostForbiddenException extends ApiException {

    public ProductPostForbiddenException(Long postId, Long memberId) {
        super(String.format("작성자와 요청자가 다릅니다. productPostId=%d, memberId=%d", postId, memberId));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return "forbidden";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
