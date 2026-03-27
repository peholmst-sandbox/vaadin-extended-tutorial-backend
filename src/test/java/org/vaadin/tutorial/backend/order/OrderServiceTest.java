package org.vaadin.tutorial.backend.order;

import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.customer.CustomerService;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointService;
import org.vaadin.tutorial.backend.product.ProductId;

import org.vaadin.tutorial.backend.product.ProductCatalogService;
import org.vaadin.tutorial.backend.product.ProductCategoryService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        var categoryService = new ProductCategoryService(Duration.ZERO);
        var productCatalogService = new ProductCatalogService(Duration.ZERO, categoryService);
        var customerService = new CustomerService(Duration.ZERO);
        var pickupPointService = new PickupPointService(Duration.ZERO);
        service = new OrderService(Duration.ZERO, customerService, pickupPointService, productCatalogService);
    }

    @Test
    void save_insertsNewOrder() {
        var order = createOrder();

        var saved = service.save(order);

        assertNotNull(saved.getOrderId());
        assertEquals(1L, saved.getVersion());
        assertEquals(new CustomerId(1), saved.getCustomerId());
        assertEquals(new PickupPointId(1), saved.getPickupPointId());
        assertEquals(1, saved.getItems().size());
    }

    @Test
    void save_updatesExistingOrder() {
        var order = createOrder();
        var saved = service.save(order);

        saved.addItem(createOrderItem(new ProductId(2), "Second Product"));
        var updated = service.save(saved);

        assertEquals(saved.getOrderId(), updated.getOrderId());
        assertEquals(2L, updated.getVersion());
        assertEquals(2, updated.getItems().size());
    }

    @Test
    void save_throwsOptimisticLockingFailureOnVersionMismatch() {
        var order = createOrder();
        var saved = service.save(order);

        saved.addItem(createOrderItem(new ProductId(2), "Second Product"));
        service.save(saved);

        saved.addItem(createOrderItem(new ProductId(3), "Third Product"));

        assertThrows(OptimisticLockingFailureException.class, () -> service.save(saved));
    }

    @Test
    void save_throwsNoSuchElementExceptionForNonExistentOrder() {
        var order = createOrder();
        order.setOrderId(new OrderId(999999L));
        order.setVersion(1L);

        assertThrows(NoSuchElementException.class, () -> service.save(order));
    }

    @Test
    void save_throwsValidationExceptionWhenCustomerIdIsNull() {
        var order = createOrder();
        order.setCustomerId(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(order));
        assertTrue(exception.getMessage().contains("customer") || exception.getMessage().contains("Customer"));
    }

    @Test
    void save_throwsValidationExceptionWhenPickupPointIdIsNull() {
        var order = createOrder();
        order.setPickupPointId(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(order));
        assertTrue(exception.getMessage().contains("pickup") || exception.getMessage().contains("Pickup"));
    }

    @Test
    void save_throwsValidationExceptionWhenItemsIsEmpty() {
        var order = createOrder();
        order.setItems(List.of());

        var exception = assertThrows(ValidationException.class, () -> service.save(order));
        assertTrue(exception.getMessage().contains("item") || exception.getMessage().contains("Item"));
    }

    @Test
    void save_preservesMultipleItems() {
        var order = createOrder();
        order.addItem(createOrderItem(new ProductId(2), "Second Product"));
        order.addItem(createOrderItem(new ProductId(3), "Third Product"));

        var saved = service.save(order);

        assertEquals(3, saved.getItems().size());
    }

    @Test
    void findById_returnsOrder() {
        var order = createOrder();
        var saved = service.save(order);

        var found = service.findById(saved.requireOrderId());

        assertTrue(found.isPresent());
        assertEquals(saved.getOrderId(), found.get().getOrderId());
    }

    @Test
    void findById_returnsEmptyForNonExistent() {
        var found = service.findById(new OrderId(999999L));

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsOrders() {
        service.save(createOrder());
        var orders = service.findAll(new Query<>()).toList();

        assertFalse(orders.isEmpty());
    }

    @Test
    void findAll_filtersCustomerId() {
        var uniqueCustomerId = new CustomerId(10001);
        var order1 = createOrder();
        order1.setCustomerId(uniqueCustomerId);
        service.save(order1);

        var order2 = createOrder();
        order2.setCustomerId(new CustomerId(10002));
        service.save(order2);

        var filter = new OrderFilter(uniqueCustomerId, null);

        var orders = service.findAll(new Query<>(filter)).toList();

        assertEquals(1, orders.size());
        assertEquals(uniqueCustomerId, orders.get(0).customerId());
    }

    @Test
    void findAll_filtersPickupPointId() {
        var uniquePickupPointId = new PickupPointId(10001);
        var order1 = createOrder();
        order1.setPickupPointId(uniquePickupPointId);
        service.save(order1);

        var order2 = createOrder();
        order2.setPickupPointId(new PickupPointId(10002));
        service.save(order2);

        var filter = new OrderFilter(null, uniquePickupPointId);

        var orders = service.findAll(new Query<>(filter)).toList();

        assertEquals(1, orders.size());
        assertEquals(uniquePickupPointId, orders.get(0).pickupPointId());
    }

    @Test
    void count_returnsCount() {
        var count = service.count(new Query<>());

        assertTrue(count > 0);
    }

    private OrderDetails createOrder() {
        var order = new OrderDetails();
        order.setCustomerId(new CustomerId(1));
        order.setPickupPointId(new PickupPointId(1));
        order.addItem(createOrderItem(new ProductId(1), "Test Product"));
        return order;
    }

    private OrderItem createOrderItem(ProductId productId, String productName) {
        return new OrderItem(
                productId,
                productName,
                new Money(new BigDecimal("99.99")),
                null,
                new Quantity(1)
        );
    }
}
