package org.vaadin.tutorial.backend.order;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;

import java.time.LocalDate;

/**
 * Filter criteria for querying orders.
 *
 * @param customerId    filter by customer (optional)
 * @param pickupPointId filter by pickup point (optional)
 * @param orderDateFrom filter by order date, inclusive lower bound (optional)
 * @param orderDateTo   filter by order date, inclusive upper bound (optional)
 */
public record OrderFilter(
        @Nullable CustomerId customerId,
        @Nullable PickupPointId pickupPointId,
        @Nullable LocalDate orderDateFrom,
        @Nullable LocalDate orderDateTo
) {
}
