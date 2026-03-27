package org.vaadin.tutorial.backend.order;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;

public record OrderListItem(
        OrderId orderId,
        CustomerId customerId,
        @Nullable String customerName,
        PickupPointId pickupPointId,
        @Nullable String pickupPointName,
        int itemCount,
        Money itemTotal
) {
}
