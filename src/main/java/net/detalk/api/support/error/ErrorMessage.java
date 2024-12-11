package net.detalk.api.support.error;

import lombok.Getter;

@Getter
public class ErrorMessage {
    private final String code;
    private final String message;
    private final Object details;

    public ErrorMessage(ErrorCode errorType) {
        this.code = errorType.getCode();
        this.message = errorType.getMessage();
        this.details = null;
    }

    public ErrorMessage(ErrorCode errorType, Object details) {
        this.code = errorType.getCode();
        this.message = errorType.getMessage();
        this.details = details;
    }
}
