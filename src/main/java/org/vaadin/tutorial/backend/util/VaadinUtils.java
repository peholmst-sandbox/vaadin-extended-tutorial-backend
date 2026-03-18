package org.vaadin.tutorial.backend.util;

import com.vaadin.flow.function.SerializableFunction;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;

import java.util.function.Function;

public final class VaadinUtils {

    private VaadinUtils() {
    }

    public static <F, S> Query<F, S> fromVaadinQuery(
            com.vaadin.flow.data.provider.Query<?, F> query,
            Function<String, S> sortPropertyMapper
    ) {
        return new Query<>(
                query.getFilter().orElse(null),
                query.getOffset(), query.getLimit(),
                query.getSortOrders().stream()
                        .map(so -> fromVaadinSortOrder(so, sortPropertyMapper))
                        .toList()
        );
    }

    private static <S> SortOrder<S> fromVaadinSortOrder(
            com.vaadin.flow.data.provider.QuerySortOrder sortOrder,
            Function<String, S> sortPropertyMapper
    ) {
        var direction = switch (sortOrder.getDirection()) {
            case ASCENDING -> SortOrder.Direction.ASCENDING;
            case DESCENDING -> SortOrder.Direction.DESCENDING;
        };
        return new SortOrder<>(sortPropertyMapper.apply(sortOrder.getSorted()), direction);
    }

    public static <F, T> SerializableFunction<F, T> nullAware(SerializableFunction<F, T> mapper) {
        return from -> from == null ? null : mapper.apply(from);
    }
}
