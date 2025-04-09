package net.detalk.api.link.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ShortLinkNotFoundException extends ApiException {

    public ShortLinkNotFoundException() {
        super("Short link not found");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "short_link_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}
