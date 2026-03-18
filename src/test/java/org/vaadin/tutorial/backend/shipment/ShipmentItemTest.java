package org.vaadin.tutorial.backend.shipment;

import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.product.ProductId;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentItemTest {

    @Test
    void constructor_acceptsValidItem() {
        var item = new ShipmentItem(new ProductId(1), new Quantity(5));

        assertEquals(new ProductId(1), item.productId());
        assertEquals(new Quantity(5), item.quantity());
    }

    @Test
    void constructor_rejectsNullProductId() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShipmentItem(null, new Quantity(1)));
    }

    @Test
    void constructor_rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShipmentItem(new ProductId(1), new Quantity(0)));
    }

    @Test
    void constructor_rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Quantity(-1));
    }

    @Test
    void equals_worksCorrectly() {
        var item1 = new ShipmentItem(new ProductId(1), new Quantity(5));
        var item2 = new ShipmentItem(new ProductId(1), new Quantity(5));
        var item3 = new ShipmentItem(new ProductId(1), new Quantity(3));

        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }
}
