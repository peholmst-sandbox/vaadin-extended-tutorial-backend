package org.vaadin.tutorial.backend.data;

public record SortOrder(String property, Direction direction) {

    public enum Direction {
        ASCENDING, DESCENDING
    }
}
