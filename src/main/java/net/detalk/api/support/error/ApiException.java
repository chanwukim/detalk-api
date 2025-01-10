package net.detalk.api.support.error;

import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import org.flywaydb.core.internal.util.JsonUtils;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {

    protected ApiException() {
    }

    protected ApiException(String message) {
        super(message);
    }

    protected ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ApiException(Throwable cause) {
        super(cause);
    }

    protected ApiException(@NonNull Map<String, Object> messageFields) {
        super(JsonUtils.toJson(messageFields));
    }

    protected ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public abstract HttpStatus getHttpStatus();

    public abstract String getErrorCode();

    public abstract boolean isNecessaryToLog();


}
