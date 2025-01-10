package net.detalk.api.domain.exception;

import net.detalk.api.support.error.ApiException;
import org.springframework.http.HttpStatus;

public class ProviderUnsupportedException extends ApiException {

    public ProviderUnsupportedException(String registrationId) {
        super(String.format("알 수 없는 OAuth Provider(registrationId: %s)입니다.", registrationId));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "provider_unsupported";
    }

    @Override
    public boolean isNecessaryToLog() {
        return true;
    }
}
