package org.vaadin.tutorial.backend.common;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value object representing a validated phone number.
 * <p>
 * Accepts phone numbers with optional country code, spaces, dashes, and parentheses.
 * Examples: "+1 555-123-4567", "(555) 123-4567", "555.123.4567", "+46 70 123 45 67"
 */
public record PhoneNumber(String value) implements Serializable {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[(]?[0-9][0-9 .()-]{6,}[0-9]$"
    );

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
