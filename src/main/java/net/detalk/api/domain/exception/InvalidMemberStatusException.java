package net.detalk.api.domain.exception;

import net.detalk.api.domain.MemberStatus;
import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidMemberStatusException extends ApiException {


    public InvalidMemberStatusException(Long memberId, MemberStatus memberStatus) {
        super(String.format("유효하지 않은 회원 상태입니다. memberId=%d, memberStatus=%s", memberId, memberStatus.name()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "invalid_member_status";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }

}
