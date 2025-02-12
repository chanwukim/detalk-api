package net.detalk.api.domain;

public class DiscordErrorMessage {

    private static final int DISCORD_MAX_LENGTH = 2000;
    private static final String TRUNCATE_SUFFIX = "... [TRUNCATED]";

    private final String endpoint;
    private final String errorClass;
    private final String errorMessage;
    private final String stackTrace;

    public DiscordErrorMessage(String endpoint, String errorClass, String errorMessage, String stackTrace) {
        this.endpoint = endpoint;
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public String toDiscordFormat() {
        String raw = String.format(
            "🛑 [%s]\n• Endpoint: %s\n• Message: %s\n• StackTrace: %s",
            errorClass, endpoint, errorMessage, stackTrace
        );
        return truncate(raw, DISCORD_MAX_LENGTH);
    }

    private String truncate(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - TRUNCATE_SUFFIX.length()) + TRUNCATE_SUFFIX;
    }

}
