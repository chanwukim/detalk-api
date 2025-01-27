package net.detalk.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.DiscordConfig;
import net.detalk.api.support.EnvironmentHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiscordServiceImpl implements DiscordService {

    private final DiscordConfig config;
    private final EnvironmentHolder env;

    private JDA jda;
    private TextChannel defaultChannel;
    private String activeProfile;

    @Override
    public void initialize() {
        try {
            jda = JDABuilder.createDefault(config.getToken())
                .setActivity(Activity.playing("알람봇"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

            // JDA가 완전히 초기화될 때까지 대기
            jda.awaitReady();

            // Profile 따라 동적으로 channelId 할당
            defaultChannel = jda.getTextChannelById(config.getChannelId());

            if (defaultChannel == null) {
                log.warn("디스코드 채널을 찾지 못했습니다. channelId={}", config.getChannelId());
            }

            activeProfile = env.getActiveProfile();

            log.info("Discord JDA initialized successfully in '{}' profile. (Channel ID: {})",
                activeProfile, config.getChannelId());

            if (activeProfile.equals("prod")) {
                sendMessage("프로덕션 환경으로 Discord봇이 성공적으로 실행되었습니다.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("JDA awaitReady was interrupted.", e);
        }

    }

    @Override
    public void sendMessage(String message) {
        if (!isReady()) {
            log.warn("JDA not initialized or channel is null. Cannot send message: {}", message);
            return;
        }
        defaultChannel.sendMessage(message).queue();
    }

    @Override
    public void sendMessage(String channelId, String message) {

        if (jda == null) {
            log.warn("JDA not initialized. Cannot send message.");
            return;
        }

        TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            log.warn("Channel not found for ID: {}", channelId);
            return;
        }

        channel.sendMessage(message).queue();
    }

    private boolean isReady() {
        return (jda != null && defaultChannel != null);
    }

}
