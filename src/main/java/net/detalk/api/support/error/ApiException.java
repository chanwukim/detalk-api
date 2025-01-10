package net.detalk.api.support.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        super(convertToJson(messageFields));
    }

    private static String convertToJson(Map<String, Object> messageFields) {
        try {
            return OBJECT_MAPPER.writeValueAsString(messageFields);
        } catch (JsonProcessingException e) {
            throw new JsonConversionException("Failed to convert message fields to JSON", e);
        }
    }

    protected ApiException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public abstract HttpStatus getHttpStatus();

    public abstract String getErrorCode();

    public abstract boolean isNecessaryToLog();


}
