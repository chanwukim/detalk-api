package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPageSizeException extends ApiException {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 20;

    public InvalidPageSizeException(int pageSize) {
        super(String.format("잘못된 페이지 크기입니다: %d (허용 범위: %d-%d)", pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE));
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
