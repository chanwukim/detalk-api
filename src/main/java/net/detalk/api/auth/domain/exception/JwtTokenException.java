package net.detalk.api.auth.domain.exception;

import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class JwtTokenException extends ApiException {

    private final ErrorCode errorCode;

    public JwtTokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.errorCode.getStatus();
    }

    @Override
    public String getErrorCode() {
        return this.errorCode.getCode();
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}
