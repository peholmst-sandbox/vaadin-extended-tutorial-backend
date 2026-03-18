package org.vaadin.tutorial.backend.shipment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.order.OrderDetails;
import org.vaadin.tutorial.backend.order.OrderId;
import org.vaadin.tutorial.backend.order.OrderItem;
import org.vaadin.tutorial.backend.order.OrderService;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;
import org.vaadin.tutorial.backend.product.ProductId;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentServiceTest {

    private ShipmentService service;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        service = new ShipmentService();
        service.setArtificialDelay(Duration.ZERO);
        orderService = new OrderService();
        orderService.setArtificialDelay(Duration.ZERO);
    }

    @Test
    void save_insertsNewShipment() {
        var shipment = createShipment(new OrderId(1));

        var saved = service.save(shipment);

        assertNotNull(saved.getShipmentId());
        assertEquals(1L, saved.getVersion());
        assertEquals(new OrderId(1), saved.getOrderId());
        assertEquals(ShipmentState.COLLECTING, saved.getState());
    }

    @Test
    void save_updatesExistingShipment() {
        var shipment = createShipment(new OrderId(1));
        var saved = service.save(shipment);

        saved.setState(ShipmentState.IN_TRANSPORT);
        var updated = service.save(saved);

        assertEquals(saved.getShipmentId(), updated.getShipmentId());
        assertEquals(2L, updated.getVersion());
        assertEquals(ShipmentState.IN_TRANSPORT, updated.getState());
    }

    @Test
    void save_throwsOptimisticLockingFailureOnVersionMismatch() {
        var shipment = createShipment(new OrderId(1));
        var saved = service.save(shipment);

        saved.setState(ShipmentState.AWAITING_TRANSPORT);
        service.save(saved);

        saved.setState(ShipmentState.IN_TRANSPORT);

        assertThrows(OptimisticLockingFailureException.class, () -> service.save(saved));
    }

    @Test
    void save_throwsNoSuchElementExceptionForNonExistentShipment() {
        var shipment = createShipment(new OrderId(1));
        shipment.setShipmentId(new ShipmentId(999999L));
        shipment.setVersion(1L);

        assertThrows(NoSuchElementException.class, () -> service.save(shipment));
    }

    @Test
    void save_throwsValidationExceptionWhenOrderIdIsNull() {
        var shipment = createShipment(new OrderId(1));
        shipment.setOrderId(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(shipment));
        assertTrue(exception.getMessage().toLowerCase().contains("order"));
    }

    @Test
    void save_throwsValidationExceptionWhenStateIsNull() {
        var shipment = createShipment(new OrderId(1));
        shipment.setState(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(shipment));
        assertTrue(exception.getMessage().toLowerCase().contains("state"));
    }

    @Test
    void save_throwsValidationExceptionWhenItemsIsEmpty() {
        var shipment = createShipment(new OrderId(1));
        shipment.setItems(List.of());

        var exception = assertThrows(ValidationException.class, () -> service.save(shipment));
        assertTrue(exception.getMessage().toLowerCase().contains("item"));
    }

    @Test
    void findById_returnsShipment() {
        var shipment = createShipment(new OrderId(1));
        var saved = service.save(shipment);

        var found = service.findById(saved.getShipmentId());

        assertTrue(found.isPresent());
        assertEquals(saved.getShipmentId(), found.get().getShipmentId());
    }

    @Test
    void findById_returnsEmptyForNonExistent() {
        var found = service.findById(new ShipmentId(999999L));

        assertTrue(found.isEmpty());
    }

    @Test
    void findByOrderId_returnsShipmentsForOrder() {
        var orderId = new OrderId(100);
        service.save(createShipment(orderId));
        service.save(createShipment(orderId));
        service.save(createShipment(new OrderId(200)));

        var shipments = service.findByOrderId(orderId);

        assertEquals(2, shipments.size());
        assertTrue(shipments.stream().allMatch(s -> orderId.equals(s.getOrderId())));
    }

    @Test
    void findAll_filtersOrderId() {
        var orderId = new OrderId(100);
        service.save(createShipment(orderId));
        service.save(createShipment(new OrderId(200)));

        var filter = new ShipmentFilter(orderId, null);
        var query = new Query<ShipmentFilter, ShipmentSortProperty>(filter, 0, 10, List.of());

        var shipments = service.findAll(query);

        assertEquals(1, shipments.size());
        assertEquals(orderId, shipments.get(0).getOrderId());
    }

    @Test
    void findAll_filtersState() {
        var shipment1 = createShipment(new OrderId(1));
        shipment1.setState(ShipmentState.COLLECTING);
        service.save(shipment1);

        var shipment2 = createShipment(new OrderId(2));
        shipment2.setState(ShipmentState.DELIVERED);
        service.save(shipment2);

        var filter = new ShipmentFilter(null, ShipmentState.DELIVERED);
        var query = new Query<ShipmentFilter, ShipmentSortProperty>(filter, 0, 10, List.of());

        var shipments = service.findAll(query);

        assertEquals(1, shipments.size());
        assertEquals(ShipmentState.DELIVERED, shipments.get(0).getState());
    }

    @Test
    void getShippedQuantities_returnsCorrectQuantities() {
        var orderId = new OrderId(100);
        var productId1 = new ProductId(1);
        var productId2 = new ProductId(2);

        // First shipment: 3 of product 1, 2 of product 2
        var shipment1 = createShipment(orderId);
        shipment1.setItems(List.of(
                new ShipmentItem(productId1, new Quantity(3)),
                new ShipmentItem(productId2, new Quantity(2))
        ));
        service.save(shipment1);

        // Second shipment: 2 more of product 1
        var shipment2 = createShipment(orderId);
        shipment2.setItems(List.of(new ShipmentItem(productId1, new Quantity(2))));
        service.save(shipment2);

        var quantities = service.getShippedQuantities(orderId);

        assertEquals(5, quantities.get(productId1));
        assertEquals(2, quantities.get(productId2));
    }

    @Test
    void getRemainingItems_returnsUnshippedItems() {
        var productId1 = new ProductId(1);
        var productId2 = new ProductId(2);

        // Create and save an order with 5 of product 1 and 3 of product 2
        var order = createAndSaveOrder(
                new OrderItem(productId1, "Product 1", new Money(new BigDecimal("10.00")), null, new Quantity(5)),
                new OrderItem(productId2, "Product 2", new Money(new BigDecimal("20.00")), null, new Quantity(3))
        );
        var orderId = order.getOrderId();

        // Ship 3 of product 1
        var shipment = createShipment(orderId);
        shipment.setItems(List.of(new ShipmentItem(productId1, new Quantity(3))));
        service.save(shipment);

        var remaining = service.getRemainingItems(order);

        assertEquals(2, remaining.size());
        // 2 remaining of product 1
        var remainingProduct1 = remaining.stream()
                .filter(i -> i.productId().equals(productId1))
                .findFirst().orElseThrow();
        assertEquals(new Quantity(2), remainingProduct1.quantity());
        // 3 remaining of product 2 (unshipped)
        var remainingProduct2 = remaining.stream()
                .filter(i -> i.productId().equals(productId2))
                .findFirst().orElseThrow();
        assertEquals(new Quantity(3), remainingProduct2.quantity());
    }

    @Test
    void getRemainingItems_excludesFullyShippedItems() {
        var productId1 = new ProductId(1);
        var productId2 = new ProductId(2);

        var order = createAndSaveOrder(
                new OrderItem(productId1, "Product 1", new Money(new BigDecimal("10.00")), null, new Quantity(2)),
                new OrderItem(productId2, "Product 2", new Money(new BigDecimal("20.00")), null, new Quantity(3))
        );
        var orderId = order.getOrderId();

        // Ship all of product 1
        var shipment = createShipment(orderId);
        shipment.setItems(List.of(new ShipmentItem(productId1, new Quantity(2))));
        service.save(shipment);

        var remaining = service.getRemainingItems(order);

        assertEquals(1, remaining.size());
        assertEquals(productId2, remaining.get(0).productId());
        assertEquals(new Quantity(3), remaining.get(0).quantity());
    }

    @Test
    void isFullyShipped_returnsTrueWhenAllShipped() {
        var productId = new ProductId(1);

        var order = createAndSaveOrder(
                new OrderItem(productId, "Product", new Money(new BigDecimal("10.00")), null, new Quantity(5))
        );
        var orderId = order.getOrderId();

        // Ship all items
        var shipment = createShipment(orderId);
        shipment.setItems(List.of(new ShipmentItem(productId, new Quantity(5))));
        service.save(shipment);

        assertTrue(service.isFullyShipped(order));
    }

    @Test
    void isFullyShipped_returnsFalseWhenPartiallyShipped() {
        var productId = new ProductId(1);

        var order = createAndSaveOrder(
                new OrderItem(productId, "Product", new Money(new BigDecimal("10.00")), null, new Quantity(5))
        );
        var orderId = order.getOrderId();

        // Ship only 3 items
        var shipment = createShipment(orderId);
        shipment.setItems(List.of(new ShipmentItem(productId, new Quantity(3))));
        service.save(shipment);

        assertFalse(service.isFullyShipped(order));
    }

    @Test
    void count_returnsCount() {
        service.save(createShipment(new OrderId(1)));
        service.save(createShipment(new OrderId(2)));

        var query = new Query<ShipmentFilter, ShipmentSortProperty>(null, 0, 10, List.of());

        var count = service.count(query);

        assertEquals(2, count);
    }

    private ShipmentDetails createShipment(OrderId orderId) {
        var shipment = new ShipmentDetails();
        shipment.setOrderId(orderId);
        shipment.setState(ShipmentState.COLLECTING);
        shipment.addItem(new ShipmentItem(new ProductId(1), new Quantity(1)));
        return shipment;
    }

    private OrderDetails createAndSaveOrder(OrderItem... items) {
        var order = new OrderDetails();
        order.setCustomerId(new CustomerId(1));
        order.setPickupPointId(new PickupPointId(1));
        for (var item : items) {
            order.addItem(item);
        }
        return orderService.save(order);
    }
}
