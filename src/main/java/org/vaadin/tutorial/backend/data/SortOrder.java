package org.vaadin.tutorial.backend.data;

/**
 * Represents a sort order for a single property.
 *
 * @param <P> the type of the sort property
 * @param property the property to sort by
 * @param direction the sort direction
 */
@Deprecated(forRemoval = true)
public record SortOrder<P>(P property, Direction direction) {

    /**
     * The direction of sorting.
     */
    public enum Direction {
        /** Sort in ascending order (smallest to largest). */
        ASCENDING,
        /** Sort in descending order (largest to smallest). */
        DESCENDING
    }
}
