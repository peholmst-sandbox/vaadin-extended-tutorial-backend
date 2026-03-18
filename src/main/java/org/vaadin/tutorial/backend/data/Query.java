package org.vaadin.tutorial.backend.data;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record Query<F, S>(
        @Nullable F filter,
        int offset,
        int limit,
        List<SortOrder<S>> sortOrders
) {

    public Query(@Nullable F filter, int offset, int limit, List<SortOrder<S>> sortOrders) {
        this.filter = filter;
        this.offset = offset;
        this.limit = limit;
        this.sortOrders = List.copyOf(sortOrders);
    }
}
