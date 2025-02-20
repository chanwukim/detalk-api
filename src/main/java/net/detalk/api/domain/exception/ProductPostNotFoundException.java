package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ProductPostNotFoundException extends ApiException {

    public ProductPostNotFoundException(Long id) {
        super(String.format("상품-게시글(ID: %d)을 찾을 수 없습니다.", id));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "product_post_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}