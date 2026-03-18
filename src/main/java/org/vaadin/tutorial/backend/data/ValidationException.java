package org.vaadin.tutorial.backend.data;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Exception thrown when bean validation fails.
 * <p>
 * Contains the set of constraint violations that caused the validation failure.
 */
public class ValidationException extends RuntimeException {

    private final Set<? extends ConstraintViolation<?>> violations;

    /**
     * Creates a new validation exception with the given constraint violations.
     *
     * @param violations the constraint violations that caused this exception
     */
    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        super(buildMessage(violations));
        this.violations = Set.copyOf(violations);
    }

    /**
     * Returns the constraint violations that caused this exception.
     *
     * @return an unmodifiable set of constraint violations
     */
    public Set<? extends ConstraintViolation<?>> getViolations() {
        return violations;
    }

    private static String buildMessage(Set<? extends ConstraintViolation<?>> violations) {
        if (violations.isEmpty()) {
            return "Validation failed";
        }
        var sb = new StringBuilder("Validation failed: ");
        var iterator = violations.iterator();
        while (iterator.hasNext()) {
            var violation = iterator.next();
            sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
