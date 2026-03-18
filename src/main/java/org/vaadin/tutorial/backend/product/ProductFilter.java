package org.vaadin.tutorial.backend.product;

import org.jspecify.annotations.Nullable;

public record ProductFilter(@Nullable String searchTerm) {
}
