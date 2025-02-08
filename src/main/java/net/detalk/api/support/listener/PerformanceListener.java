package net.detalk.api.support.listener;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.detalk.api.service.DiscordService;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@RequiredArgsConstructor
@Component
public class PerformanceListener implements ExecuteListener {

    private final DiscordService discordService;
    private static final Logger log = LoggerFactory.getLogger(PerformanceListener.class);
    private StopWatch watch;

    private static final Duration SLOW_QUERY_LIMIT= Duration.ofSeconds(5);
    private static final int MAX_MESSAGE_LENGTH = 500;
    @Override
    public void executeStart(ExecuteContext ctx) {
        watch = new StopWatch();
        watch.start("SQLQuery");
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        watch.stop();
        final long queryTimeNano = watch.getTotalTimeNanos();
        Duration executeTime = Duration.ofNanos(queryTimeNano);
        Query query = ctx.query();
    if (queryTimeNano > SLOW_QUERY_LIMIT.toNanos()) {

        String slowQueryMessage = String.format(
            """
                \n### Slow SQL 탐지
                경고: jOOQ로 실행된 쿼리 중 %d초 이상 실행된 쿼리가 있습니다.
                실행시간: %s초
                실행쿼리: %s
            """,
            SLOW_QUERY_LIMIT.toSeconds(),
            millisToSeconds(executeTime),
            query
        );


        final String trimIndicator = " ......";

        if (slowQueryMessage.length() > MAX_MESSAGE_LENGTH) {
            slowQueryMessage =
                slowQueryMessage.substring(0, MAX_MESSAGE_LENGTH - trimIndicator.length())
                    + trimIndicator;
        }

        log.warn(slowQueryMessage);
        discordService.sendMessage(slowQueryMessage);
        }
    }

    private String millisToSeconds(Duration duration) {
        return String.format("%.1f", duration.toMillis() / 1000.0);
    }
}