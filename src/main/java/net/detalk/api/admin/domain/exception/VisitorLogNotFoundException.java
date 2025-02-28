package net.detalk.api.admin.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class VisitorLogNotFoundException extends ApiException {

    public VisitorLogNotFoundException(Long id) {
        super(String.format("사용자 위치 정보를 찾을 수 없습니다. id=%d",id));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "visitor_log_not_found";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
