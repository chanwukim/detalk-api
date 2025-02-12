package net.detalk.api.service;

import net.detalk.api.domain.DiscordErrorMessage;

public interface DiscordService {

    /**
     * 톰캣 실행 시, 디스코드 봇 초기화
     */
    void initialize();

    /**
     * message 전송
     */
    void sendMessage(String message);

    /**
     * Error 전송
     */
    void sendError(DiscordErrorMessage discordErrorMessage);
}
