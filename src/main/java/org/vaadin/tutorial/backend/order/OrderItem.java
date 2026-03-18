package org.vaadin.tutorial.backend.order;

import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.product.ProductId;

import java.io.Serializable;

/**
 * Represents an item in an order.
 * <p>
 * Product details (name, price, discount) are copied from the product catalog
 * at the time the order is created, ensuring the order reflects the prices
 * at the time of purchase.
 *
 * @param productId   the ID of the product
 * @param productName the name of the product (copied from catalog)
 * @param unitPrice   the unit price of the product (copied from catalog)
 * @param discount    the discount per unit (copied from catalog, may be null)
 * @param quantity    the quantity ordered (must be at least 1)
 */
public record OrderItem(
        ProductId productId,
        String productName,
        Money unitPrice,
        @Nullable Money discount,
        Quantity quantity
) implements Serializable {

    public OrderItem {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity.value() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }

    /**
     * Calculates the total price for this item (unitPrice * quantity).
     */
    public Money totalPrice() {
        return unitPrice.multiply(quantity.value());
    }

    /**
     * Calculates the total discount for this item (discount * quantity),
     * or null if there is no discount.
     */
    public @Nullable Money totalDiscount() {
        return discount != null ? discount.multiply(quantity.value()) : null;
    }
}
