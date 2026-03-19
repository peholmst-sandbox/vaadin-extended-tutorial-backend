package org.vaadin.tutorial.backend.product;

import org.vaadin.tutorial.backend.financial.Money;

public record ProductCatalogItem(
        ProductId productId,
        String name,
        String description,
        ProductCategoryId category,
        String brand,
        Money price
) {
}
