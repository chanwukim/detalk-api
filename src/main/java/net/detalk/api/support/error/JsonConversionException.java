package net.detalk.api.support.error;

import org.springframework.http.HttpStatus;

public class JsonConversionException extends ApiException {

    public JsonConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getErrorCode() {
        return "json_conversion_failed";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}