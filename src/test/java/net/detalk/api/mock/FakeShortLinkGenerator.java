package net.detalk.api.mock;

import java.util.concurrent.atomic.AtomicLong;
import net.detalk.api.link.service.ShortLinkGenerator;

public class FakeShortLinkGenerator implements ShortLinkGenerator {

    private final AtomicLong counter = new AtomicLong(0);
    private final String prefix;

    public FakeShortLinkGenerator(String prefix) {
        this.prefix = prefix;
    }

    // 기본 생성자
    public FakeShortLinkGenerator() {
        this("fakecode");
    }

    @Override
    public String generate() {
        return prefix + counter.getAndIncrement();
    }

    // 테스트 상태 초기화를 위한 메서드
    public void reset() {
        counter.set(0);
    }
}
