package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateCreatePostException extends ApiException {

    public DuplicateCreatePostException() {
        super("이 요청은 이미 처리되었습니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "duplicate_create_post";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}