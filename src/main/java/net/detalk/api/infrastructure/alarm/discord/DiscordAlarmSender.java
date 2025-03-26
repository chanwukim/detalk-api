package net.detalk.api.infrastructure.alarm.discord;

import jakarta.annotation.PreDestroy;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.alarm.domain.AlarmErrorMessage;
import net.detalk.api.alarm.service.AlarmSender;
import net.detalk.api.support.util.EnvironmentHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordAlarmSender extends ListenerAdapter implements AlarmSender {

    private final DiscordConfig config;
    private final EnvironmentHolder env;

    private JDA jda;
    private TextChannel defaultChannel;
    private String activeProfile;

    @Override
    public void initialize() {

        log.info("Initializing Discord JDA (async)...");
        try {
            jda = JDABuilder.createLight(config.getToken())   // 최소한 기능만 사용
                .disableCache(EnumSet.allOf(CacheFlag.class)) // 디스코드 채널 메세지 관련 모든 캐시 비활성화
                .setMemberCachePolicy(MemberCachePolicy.NONE) // 디스코드 채널 멤버 정보 캐시 비활성화
                .enableIntents(EnumSet.noneOf(GatewayIntent.class)) // 디스코드 이벤트 수신 비활성화 (에러 메세지 전송만 할거임)
                .setActivity(Activity.playing("알람봇"))
                .addEventListeners(this)
                .build();

            log.info("JDA build initiated. Waiting for ReadyEvent...");

        } catch (InvalidTokenException e) {
            log.error("Failed to build JDA: Invalid Discord Bot Token! current token={}, error={}",
                config.getToken(), e.getMessage());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("JDA awaitReady was interrupted. error={}", e.getMessage());
        }

    }

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        log.info("JDA is ready! Setting up channel and profile info...");

        // JDA 준비 후 실행되어야 할 로직
        try {
            // Profile 따라 동적으로 channelId 할당
            defaultChannel = jda.getTextChannelById(config.getChannelId());

            if (defaultChannel == null) {
                log.warn("디스코드 채널을 찾지 못했습니다. channelId={}", config.getChannelId());
            } else {
                log.info("Default Discord channel found: {}", defaultChannel.getName());
            }

            activeProfile = env.getActiveProfile();

            log.info("Discord JDA initialized successfully in '{}' profile. (Channel ID: {})",
                activeProfile, config.getChannelId());

        } catch (Exception e) {
            log.error("JDA initialization failed in '{}' profile. error={}", activeProfile, e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if(jda != null) {
            log.info("Shutting down JDA...");
            jda.shutdown();

            try {
                // 5초 이상 종료 안될 시, 강제로 jda 종료
                if (jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    log.warn("JDA shutdown timed out after 10 seconds. Forcing shutdownNow...");
                    jda.shutdownNow();
                }
                log.info("JDA has been shut down successfully.");
            } catch (InterruptedException e) {
                // 대기 중 인터럽트 발생 시 처리
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for JDA shutdown. Forcing shutdownNow... error={}", e.getMessage());
                jda.shutdownNow(); // 인터럽트 시에도 강제 종료 시도
            }
        }else {
            log.info("JDA instance was null, no shutdown required.");
        }
    }

    @Async("discordAlarmExecutor")
    @Override
    public void sendMessage(String message) {
        if (!isReady()) {
            log.warn("JDA not initialized or channel is null. Cannot send message: {}", message);
            return;
        }
        sendToChannel(message);
    }

    @Async("discordAlarmExecutor")
    @Override
    public void sendError(AlarmErrorMessage message) {
        if (!isReady()) {
            log.warn("JDA not initialized or channel is null. Cannot send message: {}", message.toString());
            return;
        }
        String formattedMessage = message.toString();

        sendToChannel(formattedMessage);
    }

    private boolean isReady() {
        return (jda != null && defaultChannel != null);
    }

    private void sendToChannel(String message) {
        defaultChannel.sendMessage(message).queue(
            success -> log.debug("디스코드 알림 전송 성공"),
            failure -> log.error("디스코드 알림 전송 실패", failure)
        );
    }

}
