package net.detalk.api.support.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.domain.AlarmErrorMessage;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.support.error.ApiException;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import net.detalk.api.support.error.InvalidStateException;
import net.detalk.api.support.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

    private final AlarmSender alarmSender;

    public WebExceptionHandler(AlarmSender alarmSender) {
        this.alarmSender = alarmSender;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorMessage> handleApiException(ApiException e,
        HttpServletRequest request) {
        printNecessaryLog(e, request);


        ErrorMessage errorMessage = new ErrorMessage(e.getErrorCode(), e.getMessage());
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(errorMessage);
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorMessage> handleInvalidStateException(InvalidStateException e,
        HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorMessage(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorMessage(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorMessage> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorMessage(ErrorCode.METHOD_NOT_ALLOWED));
    }

    // Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e, HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                new ErrorMessage(
                    ErrorCode.VALIDATION_FAILED,
                    e.getBindingResult().getFieldErrors()
                        .stream()
                        .map(fieldError -> Map.of(
                            "field", fieldError.getField(),
                            "message", fieldError.getDefaultMessage()
                        ))
                        .toList()
                )
            );
    }

    // HTTP 요청의 바디(body)를 읽을 수 없을 때. 요청이 잘못된 형식이거나, 요청 바디의 내용을 매핑할 수 없는 경우 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e, HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorMessage(ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ErrorMessage> handleMethodNotAllowedException(
        MethodNotAllowedException e, HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ErrorMessage(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> handleNoResourceFoundException(NoResourceFoundException e,
        HttpServletRequest request) {
        printNecessaryLog(e, request);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorMessage(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception e, HttpServletRequest request) {

        AlarmErrorMessage alarmErrorMessage = new AlarmErrorMessage(
            request.getRequestURI(),
            e.getClass().getSimpleName(),
            getAllMessage(e),
            getStackTrace(e)
        );

        alarmSender.sendError(alarmErrorMessage);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorMessage(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private void printNecessaryLog(Exception e, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        // Next.js 요청은 로그 제외
        boolean shouldLog = userAgent != null && !userAgent.toLowerCase().contains("node");

        if (shouldLog) {
            if (e instanceof ApiException apiException) {
                if (apiException.isNecessaryToLog()) {

                    AlarmErrorMessage alarmErrorMessage = new AlarmErrorMessage(
                        request.getRequestURI(),
                        e.getClass().getSimpleName(),
                        getAllMessage(e),
                        getStackTrace(e)
                    );

                    alarmSender.sendError(alarmErrorMessage);
                    log.error("API exception. URI: {}, Method: {}, Message: {}",
                        request.getRequestURI(),
                        request.getMethod(),
                        e.getMessage()
                    );
                }
            } else {
                log.error("Runtime exception: URI: {}, Method: {}, Type: {}, Message: {}",
                    request.getRequestURI(),
                    request.getMethod(),
                    e.getClass().getSimpleName(),
                    e.getMessage()
                );
            }
        }
    }

    private String getAllMessage(Throwable e) {
        Throwable cause = e;
        StringBuilder strBuilder = new StringBuilder();

        while (cause != null && !StringUtil.isEmpty(cause.getMessage())) {
            strBuilder.append("caused: ").append(cause.getMessage()).append("; ");
            cause = cause.getCause();
        }

        return strBuilder.toString();
    }

    private String getStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace())
            .limit(5)
            .map(StackTraceElement::toString)
            .collect(Collectors.joining("\n"));
    }
}
