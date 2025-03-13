package net.detalk.api.member.domain.exception;

import net.detalk.api.member.domain.MemberStatus;
import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class MemberNeedSignUpException extends ApiException {


    public MemberNeedSignUpException(MemberStatus memberStatus) {
        super(String.format("회원가입이 필요한 외부 회원입니다. memberStatus=%s", memberStatus.name()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return "need_sign_up";
    }

    @Override
    public boolean isNecessaryToLog() {
        return false;
    }
}
