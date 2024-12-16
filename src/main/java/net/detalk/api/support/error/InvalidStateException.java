package net.detalk.api.support.error;

import lombok.Getter;

/**
 * "잘못된 상태"를 처리하는 예외입니다.
 * 시스템 오류가 아닌, 비즈니스 흐름 상에서 **허용되지 않는 상태**를 처리하는 데 적합합니다.
 * 예시:
 * - "회원의 프로필이 반드시 존재해야하는데, 존재하지 않는 상태일 때"
 */
@Getter
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}

