package net.detalk.api.infrastructure.alarm.discord;

import jakarta.annotation.PreDestroy;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean jdaReady = new AtomicBoolean(false);

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
            log.error("Failed to build JDA instance during initialization. error={}", e.getMessage());
        }

    }

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        log.info("JDA is ready! Setting up channel and profile info...");
        boolean setupSuccess = false;
        try {

            defaultChannel = jda.getTextChannelById(config.getChannelId());
            activeProfile = env.getActiveProfile();

            // 채널 찾기 성공 시 플래그 설정
            if (defaultChannel != null) {
                log.info("Default Discord channel found: {}. Profile: {}", defaultChannel.getName(), activeProfile);
                setupSuccess = true;
            } else {
                log.warn("디스코드 채널을 찾지 못했습니다. channelId={}", config.getChannelId());
            }

            // 디스코드 채널 찾았을 경우
            if (setupSuccess) {
                jdaReady.set(true);
                log.info("DiscordAlarmSender is now ready.");
                if ("prod".equals(activeProfile)) {
                    sendMessage(String.format("✅ [%s] Discord 알람 봇이 성공적으로 시작 및 준비되었습니다. (채널: %s)",
                        activeProfile.toUpperCase(), defaultChannel.getName()));
                }
                log.info("Discord 알람 봇이 성공적으로 시작 되었습니다. (채널: {}), Profile: {}",
                    defaultChannel.getName(), activeProfile);
            }else{
                log.error("Discord JDA initialization failed: Could not find the specified channel (ID: {}). Message sending will be disabled.", config.getChannelId());
            }

        } catch (Exception e) {
            log.error("JDA initialization failed in '{}' profile. error={}", activeProfile, e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if(jda != null) {
            log.info("Shutting down JDA...");
            jdaReady.set(false);
            jda.shutdown();

            try {
                // 5초 동안 종료 대기
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)) {
                    log.warn("JDA shutdown timed out after 5 seconds. Forcing shutdownNow...");
                    jda.shutdownNow();
                } else {
                    log.info("JDA has been shut down successfully within the timeout.");
                }
            } catch (InterruptedException e) {
                // 5초 지났는데도 종료 안되면 강제 종료
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for JDA shutdown. Forcing shutdownNow... error={}", e.getMessage());
                jda.shutdownNow();
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
        return jda != null && jdaReady.get() && defaultChannel != null;
    }

    private void sendToChannel(String message) {
        defaultChannel.sendMessage(message).queue(
            success -> log.debug("디스코드 알림 전송 성공"),
            failure -> log.error("디스코드 알림 전송 실패", failure)
        );
    }

}
