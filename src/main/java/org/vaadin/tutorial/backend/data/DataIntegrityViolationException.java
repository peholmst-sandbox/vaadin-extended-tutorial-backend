package org.vaadin.tutorial.backend.data;

public class DataIntegrityViolationException extends RuntimeException {

    public DataIntegrityViolationException() {
    }

    public DataIntegrityViolationException(String message) {
        super(message);
    }
}
