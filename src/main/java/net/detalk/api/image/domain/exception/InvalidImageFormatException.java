package net.detalk.api.image.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidImageFormatException extends ApiException {
    public InvalidImageFormatException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "invalid_image_format";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
