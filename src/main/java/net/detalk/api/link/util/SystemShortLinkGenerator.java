package net.detalk.api.link.util;

import java.util.Random;
import org.springframework.stereotype.Component;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/**
 * NanoID 라이브러리를 이용한 ShortLinkGenerator 구현체
 */
@Component
public class SystemShortLinkGenerator implements ShortLinkGenerator {

    private static final int CODE_LENGTH = 8;
    private static final Random DEFAULT_RANDOM = NanoIdUtils.DEFAULT_NUMBER_GENERATOR;
    private static final char[] DEFAULT_ALPHABET = NanoIdUtils.DEFAULT_ALPHABET;

    /**
     * @return 무작위로 생성된 8자 문자열
     */
    @Override
    public String generate() {
        return NanoIdUtils.randomNanoId(DEFAULT_RANDOM, DEFAULT_ALPHABET, CODE_LENGTH);
    }
}
