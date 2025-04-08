package net.detalk.api.link.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ShortLinkCreationException extends ApiException {

    public ShortLinkCreationException(String msg, Throwable error) {
        super(msg, error);
    }

    public ShortLinkCreationException(String msg) {
        super(msg);
    }

    /**
     * 단축 URL 생성에 실패했다는건 더이상 고유한 단축 URL이 없기 때문에 개발자가 직접 관여 해야 함
     * 그러므로 500 에러
     * @return INTERNAL_SERVER_ERROR
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "link_creation_failed";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
