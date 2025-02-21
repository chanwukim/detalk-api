package net.detalk.api.support;

import java.util.List;
import java.util.Objects;
import net.detalk.api.domain.exception.InvalidPageException;
import net.detalk.api.domain.exception.InvalidPageSizeException;

/**
 * 페이징 응답 클래스
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
