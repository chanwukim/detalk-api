package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ProductPostSnapshotUpdateException extends ApiException {

    public ProductPostSnapshotUpdateException(Long postId, Long newPostSnapshot) {
        super(String.format("스냅샷 업데이트에 실패했습니다. postId=%d, newPostSnapshot=%d", postId, newPostSnapshot));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "bad_request";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
