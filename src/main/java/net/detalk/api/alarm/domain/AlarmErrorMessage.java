package net.detalk.api.alarm.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 알람 시스템용 에러 메시지
 */
@Getter
@Builder
public class AlarmErrorMessage {

    private final String endpoint;
    private final String errorClass;
    private final String errorMessage;
    private final String stackTrace;

    /**
     * @param endpoint 요청 엔드포인트
     * @param errorClass 에러 클래스명
     * @param errorMessage 에러 메시지
     * @param stackTrace 스택 트레이스
     */
    public AlarmErrorMessage(String endpoint, String errorClass, String errorMessage, String stackTrace) {
        this.endpoint = endpoint;
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    /**
     * 기본 포맷으로 변환
     * @return 포맷된 문자열
     */
    public String toDefaultFormat() {
        return String.format("[%s]%nEndpoint: %s%nMessage: %s%nStackTrace: %s",
            errorClass, endpoint, errorMessage, stackTrace);
    }

    @Override
    public String toString() {
        return toDefaultFormat();
    }

}
