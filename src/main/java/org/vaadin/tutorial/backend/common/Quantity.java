package org.vaadin.tutorial.backend.common;

import java.io.Serializable;

/**
 * Value object representing a quantity of items.
 * <p>
 * Quantities must be non-negative (zero or greater).
 *
 * @param value the quantity value (must be >= 0)
 */
public record Quantity(int value) implements Serializable {

    public Quantity {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
