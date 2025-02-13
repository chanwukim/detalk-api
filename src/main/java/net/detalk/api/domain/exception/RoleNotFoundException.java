package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends ApiException {

    public RoleNotFoundException(String role) {
        super(String.format("(role: %s)에 해당하는 권한을 찾을 수 없습니다.", role));
    }

    public RoleNotFoundException() {
        super("권한을 찾을 수 없습니다.");
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "member_role_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}