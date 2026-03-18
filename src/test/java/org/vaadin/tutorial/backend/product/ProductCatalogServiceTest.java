package org.vaadin.tutorial.backend.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.data.DataIntegrityViolationException;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.financial.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogServiceTest {

    private ProductCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ProductCatalogService();
        service.setArtificialDelay(Duration.ZERO);
    }

    @Test
    void save_insertsNewProduct() {
        var product = createProduct("NEW-SKU-001");

        var saved = service.save(product);

        assertNotNull(saved.getProductId());
        assertEquals(1L, saved.getVersion());
        assertEquals("NEW-SKU-001", saved.getSku());
    }

    @Test
    void save_updatesExistingProduct() {
        var product = createProduct("NEW-SKU-002");
        var saved = service.save(product);

        saved.setName("Updated Name");
        var updated = service.save(saved);

        assertEquals(saved.getProductId(), updated.getProductId());
        assertEquals(2L, updated.getVersion());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void save_throwsExceptionForDuplicateSkuOnInsert() {
        var product1 = createProduct("DUPLICATE-SKU");
        service.save(product1);

        var product2 = createProduct("DUPLICATE-SKU");

        assertThrows(DataIntegrityViolationException.class, () -> service.save(product2));
    }

    @Test
    void save_throwsExceptionForDuplicateSkuOnUpdate() {
        var product1 = createProduct("SKU-FIRST");
        service.save(product1);

        var product2 = createProduct("SKU-SECOND");
        var saved2 = service.save(product2);

        saved2.setSku("SKU-FIRST");

        assertThrows(DataIntegrityViolationException.class, () -> service.save(saved2));
    }

    @Test
    void save_allowsUpdatingProductWithSameSku() {
        var product = createProduct("SAME-SKU");
        var saved = service.save(product);

        saved.setName("Updated Name");

        var updated = service.save(saved);

        assertEquals("SAME-SKU", updated.getSku());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void save_allowsNullSku() {
        var product = createProduct(null);

        var saved = service.save(product);

        assertNotNull(saved.getProductId());
        assertNull(saved.getSku());
    }

    @Test
    void save_allowsMultipleProductsWithNullSku() {
        var product1 = createProduct(null);
        var product2 = createProduct(null);

        var saved1 = service.save(product1);
        var saved2 = service.save(product2);

        assertNotEquals(saved1.getProductId(), saved2.getProductId());
    }

    @Test
    void save_throwsOptimisticLockingFailureOnVersionMismatch() {
        var product = createProduct("OPT-LOCK-SKU");
        var saved = service.save(product);

        saved.setName("First update");
        service.save(saved);

        saved.setName("Stale update");

        assertThrows(OptimisticLockingFailureException.class, () -> service.save(saved));
    }

    @Test
    void save_throwsNoSuchElementExceptionForNonExistentProduct() {
        var product = createProduct("NON-EXISTENT-SKU");
        product.setProductId(new ProductId(999999L));
        product.setVersion(1L);

        assertThrows(NoSuchElementException.class, () -> service.save(product));
    }

    @Test
    void findDetailsById_returnsProduct() {
        var product = createProduct("FIND-SKU");
        var saved = service.save(product);

        var found = service.findDetailsById(saved.getProductId());

        assertTrue(found.isPresent());
        assertEquals(saved.getProductId(), found.get().getProductId());
    }

    @Test
    void findDetailsById_returnsEmptyForNonExistent() {
        var found = service.findDetailsById(new ProductId(999999L));

        assertTrue(found.isEmpty());
    }

    @Test
    void findItems_returnsItems() {
        var query = new Query<ProductFilter, ProductSortProperty>(null, 0, 10, List.of());

        var items = service.findItems(query);

        assertFalse(items.isEmpty());
    }

    @Test
    void countItems_returnsCount() {
        var query = new Query<ProductFilter, ProductSortProperty>(null, 0, 10, List.of());

        var count = service.countItems(query);

        assertTrue(count > 0);
    }

    private ProductDetails createProduct(String sku) {
        var product = new ProductDetails();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setCategory("Electronics");
        product.setBrand("TestBrand");
        product.setSku(sku);
        product.setPrice(new Money(new BigDecimal("99.99")));
        return product;
    }
}
