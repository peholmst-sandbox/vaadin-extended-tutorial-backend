package org.vaadin.tutorial.backend.shipment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.order.OrderId;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the full details of a shipment.
 * <p>
 * A shipment belongs to an order and contains a subset of the order's items.
 * An order can have multiple shipments (partial shipments).
 * <p>
 * Required fields: orderId, state, items (at least one).
 */
public class ShipmentDetails {

    private @Nullable ShipmentId shipmentId;
    private @Nullable Long version;

    @NotNull(groups = OnSave.class, message = "Order is required")
    private @Nullable OrderId orderId;

    @NotNull(groups = OnSave.class, message = "State is required")
    private @Nullable ShipmentState state;

    @NotEmpty(groups = OnSave.class, message = "Shipment must contain at least one item")
    private List<ShipmentItem> items = new ArrayList<>();

    public ShipmentDetails() {
    }

    public ShipmentDetails(ShipmentDetails original) {
        this.shipmentId = original.shipmentId;
        this.version = original.version;
        this.orderId = original.orderId;
        this.state = original.state;
        this.items = new ArrayList<>(original.items);
    }

    public @Nullable ShipmentId getShipmentId() {
        return shipmentId;
    }

    void setShipmentId(ShipmentId shipmentId) {
        this.shipmentId = shipmentId;
    }

    public @Nullable Long getVersion() {
        return version;
    }

    void setVersion(Long version) {
        this.version = version;
    }

    public @Nullable OrderId getOrderId() {
        return orderId;
    }

    public void setOrderId(@Nullable OrderId orderId) {
        this.orderId = orderId;
    }

    public @Nullable ShipmentState getState() {
        return state;
    }

    public void setState(@Nullable ShipmentState state) {
        this.state = state;
    }

    public List<ShipmentItem> getItems() {
        return items;
    }

    public void setItems(List<ShipmentItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public void addItem(ShipmentItem item) {
        this.items.add(item);
    }
}
