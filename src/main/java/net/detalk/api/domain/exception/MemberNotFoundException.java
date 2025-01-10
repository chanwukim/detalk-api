package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends ApiException {

    public MemberNotFoundException(Long memberId) {
        super(String.format("회원(ID: %d)을 찾을 수 없습니다.", memberId));
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
        return true;
    }
}
