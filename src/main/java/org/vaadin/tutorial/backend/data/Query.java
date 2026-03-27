package org.vaadin.tutorial.backend.data;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Represents a query for fetching data with filtering, pagination, and sorting.
 *
 * @param <F> the type of the filter object
 * @param <S> the type of the sort property
 * @param filter the filter to apply, or {@code null} for no filtering
 * @param offset the zero-based index of the first item to return
 * @param limit the maximum number of items to return
 * @param sortOrders the sort orders to apply, in order of precedence
 */
@Deprecated(forRemoval = true)
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
