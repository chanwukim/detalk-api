package net.detalk.api.alarm.service;

import net.detalk.api.alarm.domain.AlarmErrorMessage;

/**
 * 알람 전송을 위한 인터페이스
 */
public interface AlarmSender {

    /**
     * 알람 서비스 초기화
     */
    void initialize();

    /**
     * 메시지 전송
     * @param message 전송할 메시지
     */
    void sendMessage(String message);

    /**
     * 에러 메시지 전송
     * @param errorMessage 전송할 에러 메시지
     */
    void sendError(AlarmErrorMessage errorMessage);
}
