package org.vaadin.tutorial.backend.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.data.DataIntegrityViolationException;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.financial.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogServiceTest {

    private ProductCatalogService service;
    private ProductCategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new ProductCategoryService(Duration.ZERO);
        service = new ProductCatalogService(Duration.ZERO, categoryService);
    }

    @Test
    void save_insertsNewProduct() {
        var product = createProduct("NEW-SKU-001");

        var saved = service.save(product);

        assertNotNull(saved.getProductId());
        assertEquals(1L, saved.getVersion());
        assertEquals(new SKU("NEW-SKU-001"), saved.getSku());
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

        saved2.setSku(new SKU("SKU-FIRST"));

        assertThrows(DataIntegrityViolationException.class, () -> service.save(saved2));
    }

    @Test
    void save_allowsUpdatingProductWithSameSku() {
        var product = createProduct("SAME-SKU");
        var saved = service.save(product);

        saved.setName("Updated Name");

        var updated = service.save(saved);

        assertEquals(new SKU("SAME-SKU"), updated.getSku());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void save_throwsValidationExceptionWhenNameIsNull() {
        var product = createProduct("VALID-SKU");
        product.setName(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    void save_throwsValidationExceptionWhenSkuIsNull() {
        var product = createProduct("TEMP-SKU");
        product.setSku(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("sku"));
    }

    @Test
    void save_throwsValidationExceptionWhenPriceIsNull() {
        var product = createProduct("VALID-SKU");
        product.setPrice(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("price"));
    }

    @Test
    void save_throwsValidationExceptionWhenDescriptionIsNull() {
        var product = createProduct("VALID-SKU");
        product.setDescription(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("description"));
    }

    @Test
    void save_throwsValidationExceptionWhenCategoryIsNull() {
        var product = createProduct("VALID-SKU");
        product.setCategory(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("category"));
    }

    @Test
    void save_throwsValidationExceptionWhenBrandIsNull() {
        var product = createProduct("VALID-SKU");
        product.setBrand(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(product));
        assertTrue(exception.getMessage().contains("brand"));
    }

    @Test
    void save_throwsValidationExceptionForBlankName() {
        var product = createProduct("VALID-SKU");
        product.setName("   ");

        assertThrows(ValidationException.class, () -> service.save(product));
    }

    @Test
    void save_allowsNullReleaseDate() {
        var product = createProduct("OPTIONAL-RELEASE-SKU");
        product.setReleaseDate(null);

        var saved = service.save(product);

        assertNull(saved.getReleaseDate());
    }

    @Test
    void save_allowsNullDiscount() {
        var product = createProduct("OPTIONAL-DISCOUNT-SKU");
        product.setDiscount(null);

        var saved = service.save(product);

        assertNull(saved.getDiscount());
    }

    @Test
    void save_allowsReleaseDateAndDiscountToBeSet() {
        var product = createProduct("FULL-PRODUCT-SKU");
        product.setReleaseDate(LocalDate.of(2024, 6, 15));
        product.setDiscount(new Money(new BigDecimal("10.00")));

        var saved = service.save(product);

        assertEquals(LocalDate.of(2024, 6, 15), saved.getReleaseDate());
        assertEquals(new Money(new BigDecimal("10.00")), saved.getDiscount());
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
        product.setCategory(categoryService.idOf("Electronics"));
        product.setBrand("TestBrand");
        product.setSku(new SKU(sku));
        product.setPrice(new Money(new BigDecimal("99.99")));
        return product;
    }
}
