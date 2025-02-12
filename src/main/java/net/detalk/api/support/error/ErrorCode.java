package net.detalk.api.support.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // HTTP
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error",
        "Internal Server Error. Please try again later. If the issue persists, contact support."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad_request", "Bad Request."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized. Please sign in."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden", "Forbidden. You do not have permission to access this resource."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "not_found", "Not Found."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed", "Method not allowed"),
    CONFLICT(HttpStatus.CONFLICT, "conflict", "Conflict. A conflict occurred with the current state of the resource."),
    //
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "validation_failed", "Validation failed."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "token_expired", "Token expired."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "token_invalid", "Token Invalid."),

    PROVIDER_UNSUPPORTED(HttpStatus.BAD_REQUEST, "provider_unsupported", "Unsupported provider."),

    NEED_SIGN_UP(HttpStatus.FORBIDDEN, "need_sign_up", "Need Sign Up."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
