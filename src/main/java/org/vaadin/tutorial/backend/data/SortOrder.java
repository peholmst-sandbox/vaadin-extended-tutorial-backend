package org.vaadin.tutorial.backend.data;

import java.util.List;

public sealed interface SortOrder {

    List<String> properties();

    boolean isAscending();

    boolean isDescending();

    record Ascending(List<String> properties) implements SortOrder {
        public Ascending(List<String> properties) {
            this.properties = List.copyOf(properties);
        }

        @Override
        public boolean isAscending() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
        }
    }

    record Descending(List<String> properties) implements SortOrder {
        public Descending(List<String> properties) {
            this.properties = List.copyOf(properties);
        }

        @Override
        public boolean isAscending() {
            return false;
        }

        @Override
        public boolean isDescending() {
            return true;
        }
    }
}
