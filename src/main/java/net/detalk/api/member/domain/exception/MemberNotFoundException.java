package net.detalk.api.member.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends ApiException {

    public MemberNotFoundException() {
        super("해당 회원을 찾을 수 없습니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "member_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}
