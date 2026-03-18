package org.vaadin.tutorial.backend.product;

import org.vaadin.tutorial.backend.financial.Money;

public record ProductCatalogItem(
        ProductId productId,
        String name,
        String description,
        String category,
        String brand,
        Money price
) {
    public static final String SORT_PROPERTY_NAME = "name";
    public static final String SORT_PROPERTY_DESCRIPTION = "description";
    public static final String SORT_PROPERTY_CATEGORY = "category";
    public static final String SORT_PROPERTY_BRAND = "brand";
    public static final String SORT_PROPERTY_PRICE = "price";
}
