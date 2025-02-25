package net.detalk.api.support.paging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CursorPageData<T> {

    private final List<T> items;
    private final Long nextId;
    private final boolean hasNext;

    public CursorPageData(List<T> items, Long nextId, boolean hasNext) {
        this.items = items;
        this.nextId = nextId;
        this.hasNext = hasNext;
    }

    public List<T> getItems() {
        return items;
    }

    public Long getNextId() {
        return nextId;
    }

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return hasNext;
    }

}
