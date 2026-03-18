package org.vaadin.tutorial.backend.shipment;

import org.vaadin.tutorial.backend.product.ProductId;

import java.io.Serializable;

/**
 * Represents an item included in a shipment.
 * <p>
 * References a product from the order by its product ID and specifies
 * how many units of that product are included in this shipment.
 *
 * @param productId the ID of the product being shipped
 * @param quantity  the number of units included in this shipment (must be at least 1)
 */
public record ShipmentItem(
        ProductId productId,
        int quantity
) implements Serializable {

    public ShipmentItem {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }
}
