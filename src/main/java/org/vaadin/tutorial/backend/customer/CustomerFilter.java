package org.vaadin.tutorial.backend.customer;

import org.jspecify.annotations.Nullable;

public record CustomerFilter(@Nullable String searchTerm) {
}
