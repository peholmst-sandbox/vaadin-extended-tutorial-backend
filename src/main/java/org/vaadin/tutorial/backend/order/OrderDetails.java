package org.vaadin.tutorial.backend.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents the full details of an order.
 * <p>
 * This is a mutable bean that supports two states:
 * <ul>
 *   <li><b>Draft state:</b> When created via the no-arg constructor, all fields are null/empty.
 *       This state is used during order creation.</li>
 *   <li><b>Saved state:</b> After being saved via {@link OrderService#save},
 *       required fields are guaranteed to be non-null, and {@code orderId} and
 *       {@code version} are assigned by the service.</li>
 * </ul>
 * <p>
 * Required fields: customerId, pickupPointId, items (at least one).
 */
public class OrderDetails {

    private @Nullable OrderId orderId;
    private @Nullable Long version;

    @NotNull(groups = OnSave.class, message = "Customer is required")
    private @Nullable CustomerId customerId;

    @NotNull(groups = OnSave.class, message = "Pickup point is required")
    private @Nullable PickupPointId pickupPointId;

    @NotEmpty(groups = OnSave.class, message = "Order must contain at least one item")
    private List<OrderItem> items = new ArrayList<>();

    public OrderDetails() {
    }

    public OrderDetails(OrderDetails original) {
        this.orderId = original.orderId;
        this.version = original.version;
        this.customerId = original.customerId;
        this.pickupPointId = original.pickupPointId;
        this.items = new ArrayList<>(original.items);
    }

    public OrderId requireOrderId() {
        return requireNonNull(orderId);
    }

    public @Nullable OrderId getOrderId() {
        return orderId;
    }

    void setOrderId(OrderId orderId) {
        this.orderId = orderId;
    }

    public @Nullable Long getVersion() {
        return version;
    }

    Long nextVersion() {
        return version == null ? 1 : version + 1;
    }

    void setVersion(Long version) {
        this.version = version;
    }

    public @Nullable CustomerId getCustomerId() {
        return customerId;
    }

    public void setCustomerId(@Nullable CustomerId customerId) {
        this.customerId = customerId;
    }

    public @Nullable PickupPointId getPickupPointId() {
        return pickupPointId;
    }

    public void setPickupPointId(@Nullable PickupPointId pickupPointId) {
        this.pickupPointId = pickupPointId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items);
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}
