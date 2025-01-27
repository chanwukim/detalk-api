package net.detalk.api.service;

public interface DiscordService {

    /**
     * 톰캣 실행 시, 디스코드 봇 초기화
     */
    void initialize();

    /**
     * 기본 채널에 message 전송
     */
    void sendMessage(String message);

    /**
     * 디스코드 특정 채널에 message 전송
     */
    void sendMessage(String channelId, String message);
}
