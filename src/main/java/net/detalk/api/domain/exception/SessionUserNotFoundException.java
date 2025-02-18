package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class SessionUserNotFoundException extends ApiException {

    public SessionUserNotFoundException() {
        super("User session not found. Please log in");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return "session_user_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
