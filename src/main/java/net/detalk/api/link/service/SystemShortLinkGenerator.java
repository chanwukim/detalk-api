package net.detalk.api.link.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * NanoID 라이브러리를 이용한 ShortLinkGenerator 구현체
 */
@Component
public class SystemShortLinkGenerator implements ShortLinkGenerator {

    // 생성할 단축 코드의 길이
    private static final int CODE_LENGTH = 8;
    private static final Random DEFAULT_RANDOM = NanoIdUtils.DEFAULT_NUMBER_GENERATOR;
    private static final char[] DEFAULT_ALPHABET = NanoIdUtils.DEFAULT_ALPHABET;

    @Override
    public String generate() {
        return NanoIdUtils.randomNanoId(DEFAULT_RANDOM, DEFAULT_ALPHABET, CODE_LENGTH);
    }
}
