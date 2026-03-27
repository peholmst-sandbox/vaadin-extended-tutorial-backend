package org.vaadin.tutorial.backend.customer;

import java.io.Serializable;

public record CustomerId(long id) implements Serializable {

    @Override
    public String toString() {
        return Long.toString(id);
    }
}
