package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ProductPostForbiddenException extends ApiException {

    public ProductPostForbiddenException() {
        super("제품 게시물 수정 권한이 없습니다.");
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
