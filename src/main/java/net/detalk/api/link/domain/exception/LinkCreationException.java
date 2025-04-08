package net.detalk.api.link.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class LinkCreationException extends ApiException {

    public LinkCreationException(String message) {
        super(message);
    }

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
