package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends ApiException {

    public RefreshTokenNotFoundException(String tokenIdentifier) {
        super(String.format("리프레시 토큰을 찾을 수 없습니다: %s", tokenIdentifier));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "refresh_token_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
