package org.vaadin.tutorial.backend.util;

import com.vaadin.flow.function.SerializableFunction;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;

import java.util.List;

public final class VaadinUtils {

    private VaadinUtils() {
    }

    public static <F> Query<F> fromVaadinQuery(com.vaadin.flow.data.provider.Query<?, F> query) {
        return new Query<>(
                query.getFilter().orElse(null),
                query.getOffset(), query.getLimit(),
                query.getSortOrders().stream().map(VaadinUtils::fromVaadinSortOrder).toList()
        );
    }

    private static SortOrder fromVaadinSortOrder(com.vaadin.flow.data.provider.QuerySortOrder sortOrder) {
        return switch (sortOrder.getDirection()) {
            case ASCENDING -> new SortOrder.Ascending(List.of(sortOrder.getSorted()));
            case DESCENDING -> new SortOrder.Descending(List.of(sortOrder.getSorted()));
        };
    }

    public static <F, T> SerializableFunction<F, T> nullAware(SerializableFunction<F, T> mapper) {
        return from -> from == null ? null : mapper.apply(from);
    }
}
