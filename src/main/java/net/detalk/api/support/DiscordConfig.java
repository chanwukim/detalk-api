package net.detalk.api.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "discord.bot")
public class DiscordConfig {
    private String token;
    private String channelId;
}