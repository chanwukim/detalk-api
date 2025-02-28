package net.detalk.api.auth.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenUnauthorizedException extends ApiException {

    public RefreshTokenUnauthorizedException() {
        super();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "unauthorized";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
