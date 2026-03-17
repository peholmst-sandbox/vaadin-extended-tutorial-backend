package org.vaadin.tutorial.backend.product;

import org.vaadin.tutorial.backend.data.Query;

import java.util.List;
import java.util.Optional;

public class ProductCatalogService {

    public List<ProductCatalogItem> findItems(Query<ProductFilter> query) {
        return List.of();
    }

    public int countItems(Query<ProductFilter> query) {
        return 0;
    }

    public Optional<ProductDetails> findDetailsById(ProductId id) {
        return Optional.empty();
    }

    public Optional<ProductCatalogItem> findItemById(ProductId id) {
        return Optional.empty();
    }

    public ProductDetails save(ProductDetails productDetails) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
