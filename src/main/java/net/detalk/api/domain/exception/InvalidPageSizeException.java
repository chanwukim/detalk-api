package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPageSizeException extends ApiException {

    public InvalidPageSizeException(int pageSize) {
        super(String.format("잘못된 페이지 사이즈 요청입니다: [%d], 페이지 사이즈는 1 이상이어야 합니다.", pageSize));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "invalid_page_size";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }

}
