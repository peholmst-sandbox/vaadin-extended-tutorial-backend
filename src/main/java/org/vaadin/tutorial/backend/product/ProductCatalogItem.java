package org.vaadin.tutorial.backend.product;

import org.vaadin.tutorial.backend.financial.Money;

public record ProductCatalogItem(
        ProductId productId,
        String name,
        String description,
        ProductCategory category,
        String brand,
        SKU sku,
        Money price
) {
}
