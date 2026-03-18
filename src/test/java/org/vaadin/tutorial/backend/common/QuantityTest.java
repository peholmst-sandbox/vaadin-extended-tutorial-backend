package org.vaadin.tutorial.backend.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @Test
    void constructor_acceptsZero() {
        var quantity = new Quantity(0);
        assertEquals(0, quantity.value());
    }

    @Test
    void constructor_acceptsPositiveValue() {
        var quantity = new Quantity(42);
        assertEquals(42, quantity.value());
    }

    @Test
    void constructor_rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity(-1));
    }

    @Test
    void toString_returnsStringValue() {
        var quantity = new Quantity(10);
        assertEquals("10", quantity.toString());
    }

    @Test
    void equals_worksCorrectly() {
        var q1 = new Quantity(5);
        var q2 = new Quantity(5);
        var q3 = new Quantity(10);

        assertEquals(q1, q2);
        assertNotEquals(q1, q3);
    }

    @Test
    void hashCode_isConsistentWithEquals() {
        var q1 = new Quantity(5);
        var q2 = new Quantity(5);

        assertEquals(q1.hashCode(), q2.hashCode());
    }
}
