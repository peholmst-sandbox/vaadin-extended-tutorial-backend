package org.vaadin.tutorial.backend.data;

public class OptimisticLockingFailureException extends RuntimeException {

    public OptimisticLockingFailureException() {
    }

    public OptimisticLockingFailureException(String message) {
        super(message);
    }
}
