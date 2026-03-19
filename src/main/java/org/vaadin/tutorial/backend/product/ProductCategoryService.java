package org.vaadin.tutorial.backend.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.common.TutorialBackendService;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductCategoryService extends TutorialBackendService {

    private final SequencedMap<ProductCategoryId, ProductCategory> categories = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public ProductCategoryService(@Value("${tutorial.backend.artificial-delay:PT0.2S}") Duration artificialDelay) {
        super(artificialDelay);
        generateTestData();
    }

    public List<ProductCategory> findAll() {
        simulateDelay();
        return List.copyOf(categories.values());
    }

    public Optional<ProductCategory> findById(ProductCategoryId id) {
        simulateDelay();
        return Optional.ofNullable(categories.get(id));
    }

    Optional<ProductCategory> getById(ProductCategoryId id) {
        return Optional.ofNullable(categories.get(id));
    }

    ProductCategoryId idOf(String categoryName) {
        return categories.values().stream()
                .filter(c -> c.name().equals(categoryName))
                .map(ProductCategory::productCategoryId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + categoryName));
    }

    private void generateTestData() {
        var names = List.of(
                "Electronics", "Clothing", "Home & Garden", "Sports & Outdoors",
                "Books", "Toys & Games", "Food & Beverages", "Health & Beauty",
                "Automotive", "Office Supplies"
        );
        for (var name : names) {
            var id = new ProductCategoryId(nextId.getAndIncrement());
            categories.put(id, new ProductCategory(id, name));
        }
    }
}
