package org.vaadin.tutorial.backend.order;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;

import java.time.LocalDate;

public record OrderListItem(
        OrderId orderId,
        LocalDate orderDate,
        CustomerId customerId,
        @Nullable String customerName,
        PickupPointId pickupPointId,
        @Nullable String pickupPointName,
        int itemCount,
        Money itemTotal
) {
}
