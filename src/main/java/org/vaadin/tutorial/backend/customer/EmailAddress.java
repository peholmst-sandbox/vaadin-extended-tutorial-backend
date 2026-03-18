package org.vaadin.tutorial.backend.customer;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 */
public record EmailAddress(String value) implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+'+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
