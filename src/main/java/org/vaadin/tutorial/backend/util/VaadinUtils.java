package org.vaadin.tutorial.backend.util;

import com.vaadin.flow.function.SerializableFunction;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;

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
        var direction = switch (sortOrder.getDirection()) {
            case ASCENDING -> SortOrder.Direction.ASCENDING;
            case DESCENDING -> SortOrder.Direction.DESCENDING;
        };
        return new SortOrder(sortOrder.getSorted(), direction);
    }

    public static <F, T> SerializableFunction<F, T> nullAware(SerializableFunction<F, T> mapper) {
        return from -> from == null ? null : mapper.apply(from);
    }
}
