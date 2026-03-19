package org.vaadin.tutorial.backend.product;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Stock Keeping Unit identifier for a product.
 * <p>
 * Must consist of uppercase alphanumeric segments separated by hyphens (e.g. {@code SKU-0001},
 * {@code ABC-123-XYZ}). Minimum length is 2 characters.
 */
public record SKU(String value) implements Serializable {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9]+(-[A-Z0-9]+)*$");

    public SKU {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
        if (value.length() < 2) {
            throw new IllegalArgumentException("SKU must be at least 2 characters: " + value);
        }
        if (!SKU_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SKU format: " + value
                    + ". Must contain only uppercase letters, digits, and hyphens (e.g. SKU-0001)");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
