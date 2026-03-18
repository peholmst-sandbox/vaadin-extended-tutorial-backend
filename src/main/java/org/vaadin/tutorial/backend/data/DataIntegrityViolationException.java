package org.vaadin.tutorial.backend.data;

/**
 * Exception thrown when a data integrity constraint is violated.
 * <p>
 * This typically occurs when attempting to insert or update data that would
 * violate a unique constraint, foreign key constraint, or other database integrity rule.
 */
public class DataIntegrityViolationException extends RuntimeException {

    /**
     * Constructs a new exception with no detail message.
     */
    public DataIntegrityViolationException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DataIntegrityViolationException(String message) {
        super(message);
    }
}
