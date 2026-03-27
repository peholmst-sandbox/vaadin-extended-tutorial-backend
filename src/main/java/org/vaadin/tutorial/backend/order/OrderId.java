package org.vaadin.tutorial.backend.order;

import java.io.Serializable;

public record OrderId(long id) implements Serializable {

    @Override
    public String toString() {
        return Long.toString(id);
    }
}
