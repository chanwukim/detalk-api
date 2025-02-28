package net.detalk.api.member.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class MemberProfileNotFoundException extends ApiException {

    public MemberProfileNotFoundException(String userhandle) {
        super(String.format("(userhandle: %s)에 해당하는 회원 프로필을 찾을 수 없습니다.", userhandle));
    }

    public MemberProfileNotFoundException() {
        super("회원 프로필을 찾을 수 없습니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "member_profile_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}