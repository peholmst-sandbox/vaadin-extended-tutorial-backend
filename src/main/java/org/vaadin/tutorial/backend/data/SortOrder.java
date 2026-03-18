package org.vaadin.tutorial.backend.data;

public record SortOrder<P>(P property, Direction direction) {

    public enum Direction {
        ASCENDING, DESCENDING
    }
}
