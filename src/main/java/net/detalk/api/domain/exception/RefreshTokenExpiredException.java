package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredException extends ApiException {

    public RefreshTokenExpiredException() {
        super("만료된 리프레시 토큰입니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "refresh_token_expired";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }

}
