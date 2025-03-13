package net.detalk.api.support.web.listener;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.detalk.api.alarm.service.AlarmSender;
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

    // 직렬화 제외
    // 직렬화가 불가능한 Thread, Network 관련 코드가 있음 -> NotSerializableException 발생 가능성
    private transient final AlarmSender alarmSender;
    private transient final ThreadLocal<StopWatch> watch = ThreadLocal.withInitial(() -> null);

    private static final Logger log = LoggerFactory.getLogger(PerformanceListener.class);
    private static final Duration SLOW_QUERY_LIMIT= Duration.ofSeconds(5);
    private static final int MAX_MESSAGE_LENGTH = 500;

    @Override
    public void executeStart(ExecuteContext ctx) {
        StopWatch newWatch = new StopWatch();
        newWatch.start("SQLQuery");
        watch.set(newWatch);
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        // ThreadLocal에서 꺼냄
        StopWatch sw = watch.get();
        try {
            if (sw == null) {
                // StopWatch가 생성되지 않았으면 로그만 남기고 종료
                log.warn("StopWatch가 null입니다. 정상적으로 시작되지 않았을 수 있습니다.");
                return;
            }

            // 실행 중이면 stop()
            if (sw.isRunning()) {
                sw.stop();
            }

            // 소요 시간 계산
            final long queryTimeNano = sw.getTotalTimeNanos();
            Duration executeTime = Duration.ofNanos(queryTimeNano);
            Query query = ctx.query();

            // 슬로우 쿼리 검사
            if (queryTimeNano > SLOW_QUERY_LIMIT.toNanos()) {

                String slowQueryMessage = String.format(
                    """
                            ### Slow SQL 탐지
                            경고: jOOQ로 실행된 쿼리 중 %d초 이상 실행된 쿼리가 있습니다.
                            실행시간: %s초
                            실행쿼리: %s
                        """,
                    SLOW_QUERY_LIMIT.toSeconds(),
                    millisToSeconds(executeTime),
                    query
                );

                final String trimIndicator = " ......";

                // 메시지 길이 제한
                if (slowQueryMessage.length() > MAX_MESSAGE_LENGTH) {
                    slowQueryMessage =
                        slowQueryMessage.substring(0, MAX_MESSAGE_LENGTH - trimIndicator.length())
                            + trimIndicator;
                }

                log.warn(slowQueryMessage);
                alarmSender.sendMessage(slowQueryMessage);
            }
        } catch (Exception e) {
            log.error("쿼리 실행 시간 측정 중 오류 발생");
        }finally {
            // 한 번 사용 후, ThreadLocal에서 제거
            log.debug("ThreadLocal StopWatch 정리 완료");
            watch.remove();
        }
}

    private String millisToSeconds(Duration duration) {
        return String.format("%.1f", duration.toMillis() / 1000.0);
    }
}