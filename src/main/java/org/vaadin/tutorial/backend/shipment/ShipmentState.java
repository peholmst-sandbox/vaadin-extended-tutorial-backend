package org.vaadin.tutorial.backend.shipment;

/**
 * Represents the current state of a shipment in its lifecycle.
 */
public enum ShipmentState {

    /**
     * Items are being collected for this shipment.
     */
    COLLECTING,

    /**
     * Shipment is packed and waiting for transport pickup.
     */
    AWAITING_TRANSPORT,

    /**
     * Shipment is in transit to the pickup point.
     */
    IN_TRANSPORT,

    /**
     * Shipment has been delivered to the pickup point.
     */
    DELIVERED,

    /**
     * Shipment has been picked up by the customer.
     */
    PICKED_UP,

    /**
     * Shipment was returned (customer did not pick up, refused, etc.).
     */
    RETURNED,

    /**
     * Shipment was lost during transport or at the pickup point.
     */
    LOST
}
