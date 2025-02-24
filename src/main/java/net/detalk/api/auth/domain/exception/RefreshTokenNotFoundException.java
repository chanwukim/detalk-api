package net.detalk.api.auth.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends ApiException {

    public RefreshTokenNotFoundException() {
        super("존재하지 않는 리프레시 토큰입니다.");
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
