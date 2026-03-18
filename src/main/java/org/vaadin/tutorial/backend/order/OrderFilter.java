package org.vaadin.tutorial.backend.order;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;

/**
 * Filter criteria for querying orders.
 *
 * @param customerId    filter by customer (optional)
 * @param pickupPointId filter by pickup point (optional)
 */
public record OrderFilter(
        @Nullable CustomerId customerId,
        @Nullable PickupPointId pickupPointId
) {
}
