package org.vaadin.tutorial.backend.shipment;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.order.OrderId;

/**
 * Filter criteria for querying shipments.
 *
 * @param orderId filter by order (optional)
 * @param state   filter by shipment state (optional)
 */
public record ShipmentFilter(
        @Nullable OrderId orderId,
        @Nullable ShipmentState state
) {
}
