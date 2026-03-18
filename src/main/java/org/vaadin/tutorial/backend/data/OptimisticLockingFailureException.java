package org.vaadin.tutorial.backend.data;

/**
 * Exception thrown when an update fails due to optimistic locking.
 * <p>
 * This typically occurs when attempting to update an entity that has been
 * modified by another transaction since it was read.
 */
public class OptimisticLockingFailureException extends RuntimeException {

    /**
     * Constructs a new exception with no detail message.
     */
    public OptimisticLockingFailureException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public OptimisticLockingFailureException(String message) {
        super(message);
    }
}
