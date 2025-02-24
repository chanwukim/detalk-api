package net.detalk.api.admin.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class VisitorLocationSaveException extends ApiException {

    public VisitorLocationSaveException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "visitor_location_save_failed";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }

}
