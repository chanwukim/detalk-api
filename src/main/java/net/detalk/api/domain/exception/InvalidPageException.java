package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPageException extends ApiException {

    private final static int MIN_PAGE = 0;
    private final static int MAX_PAGE = 1000;

    public InvalidPageException(int requestedPage) {
        super(String.format(
            "요청한 페이지 번호가 유효하지 않습니다: %d (허용 범위: %d~%d)",
            requestedPage,
            MIN_PAGE,
            MAX_PAGE
        ));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "invalid_page_number";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }

}
