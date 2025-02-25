package net.detalk.api.support.paging;

import java.util.List;
import java.util.Objects;
import net.detalk.api.domain.exception.InvalidPageException;
import net.detalk.api.domain.exception.InvalidPageSizeException;

/**
 * 페이징 응답 클래스
 * @param content 페이징할 데이터
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지당 항목 수
 * @param totalElements 전체 데이터 수
 * @param totalPages 전체 페이지 수
 */
public record PagingData<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

    public PagingData {
        Objects.requireNonNull(content, "Content must not be null");
        if (page < 0) {
            throw new InvalidPageException(this.page());
        }
        if (size < 1) {
            throw new InvalidPageSizeException(this.size());
        }
    }

}
