package org.vaadin.tutorial.backend.data;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record Query<F>(
        @Nullable F filter,
        int offset,
        int limit,
        List<SortOrder> sortOrders
) {

    public Query(@Nullable F filter, int offset, int limit, List<SortOrder> sortOrders) {
        this.filter = filter;
        this.offset = offset;
        this.limit = limit;
        this.sortOrders = List.copyOf(sortOrders);
    }
}
