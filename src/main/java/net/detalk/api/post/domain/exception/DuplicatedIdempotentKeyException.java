package net.detalk.api.post.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatedIdempotentKeyException extends ApiException {

    public DuplicatedIdempotentKeyException() {
        super("이 요청은 이미 처리되었습니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "duplicate_idempotent_key";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}