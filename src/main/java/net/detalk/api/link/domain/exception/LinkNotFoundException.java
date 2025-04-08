package net.detalk.api.link.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class LinkNotFoundException extends ApiException {

    public LinkNotFoundException(String message) {
        super("Requested short link does not exist : " + message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "link_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}
