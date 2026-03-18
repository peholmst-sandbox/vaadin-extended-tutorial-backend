package org.vaadin.tutorial.backend.order;

import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.product.ProductId;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void constructor_acceptsValidItem() {
        var item = new OrderItem(
                new ProductId(1),
                "Test Product",
                new Money(new BigDecimal("10.00")),
                new Money(new BigDecimal("1.00")),
                2
        );

        assertEquals(new ProductId(1), item.productId());
        assertEquals("Test Product", item.productName());
        assertEquals(new Money(new BigDecimal("10.00")), item.unitPrice());
        assertEquals(new Money(new BigDecimal("1.00")), item.discount());
        assertEquals(2, item.quantity());
    }

    @Test
    void constructor_acceptsNullDiscount() {
        var item = new OrderItem(
                new ProductId(1),
                "Test Product",
                new Money(new BigDecimal("10.00")),
                null,
                1
        );

        assertNull(item.discount());
    }

    @Test
    void constructor_rejectsNullProductId() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(null, "Test", new Money(BigDecimal.TEN), null, 1));
    }

    @Test
    void constructor_rejectsNullProductName() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(new ProductId(1), null, new Money(BigDecimal.TEN), null, 1));
    }

    @Test
    void constructor_rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(new ProductId(1), "   ", new Money(BigDecimal.TEN), null, 1));
    }

    @Test
    void constructor_rejectsNullUnitPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(new ProductId(1), "Test", null, null, 1));
    }

    @Test
    void constructor_rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(new ProductId(1), "Test", new Money(BigDecimal.TEN), null, 0));
    }

    @Test
    void constructor_rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(new ProductId(1), "Test", new Money(BigDecimal.TEN), null, -1));
    }

    @Test
    void totalPrice_calculatesCorrectly() {
        var item = new OrderItem(
                new ProductId(1),
                "Test Product",
                new Money(new BigDecimal("10.00")),
                null,
                3
        );

        assertEquals(new Money(new BigDecimal("30.00")), item.totalPrice());
    }

    @Test
    void totalDiscount_calculatesCorrectlyWhenPresent() {
        var item = new OrderItem(
                new ProductId(1),
                "Test Product",
                new Money(new BigDecimal("10.00")),
                new Money(new BigDecimal("1.50")),
                3
        );

        assertEquals(new Money(new BigDecimal("4.50")), item.totalDiscount());
    }

    @Test
    void totalDiscount_returnsNullWhenNoDiscount() {
        var item = new OrderItem(
                new ProductId(1),
                "Test Product",
                new Money(new BigDecimal("10.00")),
                null,
                3
        );

        assertNull(item.totalDiscount());
    }
}
