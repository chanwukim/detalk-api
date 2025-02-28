package net.detalk.api.member.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class UserHandleDuplicatedException extends ApiException {

    public UserHandleDuplicatedException(String userHandle) {
        super(String.format("이미 존재하는 userhandle입니다: %s", userHandle));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "user_handle_conflict";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
