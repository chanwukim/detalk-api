package net.detalk.api.support.util;

import java.security.SecureRandom;

public class StringUtil {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    private StringUtil() {}

    public static String generateUpperAndNumber(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("길이는 1 이상이어야 합니다.");
        }

        String upperAndNumber = CHAR_UPPER + NUMBER;
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(upperAndNumber.length());
            sb.append(upperAndNumber.charAt(rndCharAt));
        }

        return sb.toString();
    }

    public static String generateLowerAndNumber(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("길이는 1 이상이어야 합니다.");
        }

        String lowerAndNumber = CHAR_LOWER + NUMBER;
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(lowerAndNumber.length());
            sb.append(lowerAndNumber.charAt(rndCharAt));
        }

        return sb.toString();
    }

    public static String generateMixedCaseAndNumber(int length) {
        if (length < 3) {
            throw new IllegalArgumentException("길이는 3 이상이어야 합니다.");
        }

        // 각 문자 타입에서 최소 1개 보장
        char lowerChar = CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length()));
        char upperChar = CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length()));
        char numberChar = NUMBER.charAt(random.nextInt(NUMBER.length()));

        // 나머지 문자는 랜덤하게 채움
        String mixedChars = CHAR_LOWER + CHAR_UPPER + NUMBER;
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < length - 3; i++) {
            int rndCharAt = random.nextInt(mixedChars.length());
            randomPart.append(mixedChars.charAt(rndCharAt));
        }

        // 문자열 섞기
        String combinedString = lowerChar + upperChar + numberChar + randomPart.toString();
        return shuffleString(combinedString);
    }

    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[index];
            characters[index] = characters[i];
            characters[i] = temp;
        }
        return new String(characters);
    }

    /**
     * <p>문자열이 빈 문자열("")이거나 null인지 확인합니다.</p>
     *
     * @param str 확인할 문자열, null일 수 있음
     * @return 문자열이 빈 문자열이거나 null일 경우 {@code true}
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * <p>문자열이 빈 문자열("") 또는 null이 아닌지 확인합니다.</p>
     *
     * @param str 확인할 문자열, null일 수 있음
     * @return 문자열이 빈 문자열이 아니고 null이 아닐 경우 {@code true}
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
