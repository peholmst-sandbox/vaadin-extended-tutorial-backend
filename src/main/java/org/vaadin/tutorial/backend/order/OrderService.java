package org.vaadin.tutorial.backend.order;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.common.TutorialBackendService;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.customer.CustomerId;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;
import org.vaadin.tutorial.backend.product.ProductCatalogService;
import org.vaadin.tutorial.backend.product.ProductFilter;
import org.vaadin.tutorial.backend.product.ProductSortProperty;

@Service
public class OrderService extends TutorialBackendService {

    private final ConcurrentHashMap<OrderId, OrderDetails> orders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Validator validator;
    private final ProductCatalogService productCatalogService;

    public OrderService(@Value("${tutorial.backend.artificial-delay:PT0.2S}") Duration artificialDelay,
                        ProductCatalogService productCatalogService) {
        super(artificialDelay);
        this.productCatalogService = productCatalogService;
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        generateTestData();
    }

    public List<OrderDetails> findAll(Query<OrderFilter, OrderSortProperty> query) {
        simulateDelay();
        return filteredStream(query.filter())
                .sorted(buildComparator(query.sortOrders()))
                .skip(query.offset())
                .limit(query.limit())
                .map(OrderDetails::new)
                .toList();
    }

    public int count(Query<OrderFilter, OrderSortProperty> query) {
        simulateDelay();
        return (int) filteredStream(query.filter()).count();
    }

    public Optional<OrderDetails> findById(OrderId id) {
        simulateDelay();
        return Optional.ofNullable(orders.get(id)).map(OrderDetails::new);
    }

    public OrderDetails save(OrderDetails orderDetails) {
        simulateDelay();
        validate(orderDetails);
        if (orderDetails.getOrderId() == null) {
            return insert(orderDetails);
        } else {
            return update(orderDetails);
        }
    }

    private void validate(OrderDetails orderDetails) {
        var violations = validator.validate(orderDetails, OnSave.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private OrderDetails insert(OrderDetails orderDetails) {
        var id = new OrderId(nextId.getAndIncrement());
        var saved = new OrderDetails(orderDetails);
        saved.setOrderId(id);
        saved.setVersion(1L);
        orders.put(id, saved);
        return new OrderDetails(saved);
    }

    private OrderDetails update(OrderDetails orderDetails) {
        assert orderDetails.getOrderId() != null;
        var result = orders.compute(orderDetails.getOrderId(), (id, existing) -> {
            if (existing == null) {
                throw new NoSuchElementException("Order not found: " + id);
            }
            if (!Objects.equals(existing.getVersion(), orderDetails.getVersion())) {
                throw new OptimisticLockingFailureException();
            }
            var updated = new OrderDetails(orderDetails);
            updated.setOrderId(id);
            updated.setVersion(existing.getVersion() + 1);
            return updated;
        });
        return new OrderDetails(result);
    }

    private Stream<OrderDetails> filteredStream(@Nullable OrderFilter filter) {
        var stream = orders.values().stream();
        if (filter != null) {
            if (filter.customerId() != null) {
                stream = stream.filter(o -> filter.customerId().equals(o.getCustomerId()));
            }
            if (filter.pickupPointId() != null) {
                stream = stream.filter(o -> filter.pickupPointId().equals(o.getPickupPointId()));
            }
        }
        return stream;
    }

    private static Comparator<OrderDetails> buildComparator(List<SortOrder<OrderSortProperty>> sortOrders) {
        Comparator<OrderDetails> comparator = null;
        for (var sortOrder : sortOrders) {
            Comparator<OrderDetails> propertyComparator = propertyComparator(sortOrder.property());
            if (sortOrder.direction() == SortOrder.Direction.DESCENDING) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(o -> o.getOrderId().id());
    }

    @SuppressWarnings("unchecked")
    private static Comparator<OrderDetails> propertyComparator(OrderSortProperty property) {
        return Comparator.comparing(o -> (Comparable<Object>) getProperty(o, property), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparable<?> getProperty(OrderDetails order, OrderSortProperty property) {
        return switch (property) {
            case ORDER_ID -> order.getOrderId() != null ? order.getOrderId().id() : null;
            case CUSTOMER_ID -> order.getCustomerId() != null ? order.getCustomerId().id() : null;
            case PICKUP_POINT_ID -> order.getPickupPointId() != null ? order.getPickupPointId().id() : null;
            case ITEM_COUNT -> order.getItems().size();
        };
    }

    private void generateTestData() {
        var random = new Random(789);
        var products = productCatalogService.findItems(
                new Query<>(null, 0, Integer.MAX_VALUE, List.of()));

        for (int i = 0; i < 200; i++) {
            var order = new OrderDetails();
            var id = new OrderId(nextId.getAndIncrement());
            order.setOrderId(id);
            order.setVersion(1L);
            order.setCustomerId(new CustomerId(1 + random.nextInt(100)));
            order.setPickupPointId(new PickupPointId(1 + random.nextInt(50)));

            int itemCount = 1 + random.nextInt(5);
            for (int j = 0; j < itemCount; j++) {
                var product = products.get(random.nextInt(products.size()));
                var details = productCatalogService.findDetailsById(product.productId()).orElseThrow();
                order.addItem(new OrderItem(
                        product.productId(),
                        product.name(),
                        product.price(),
                        details.getDiscount(),
                        new Quantity(1 + random.nextInt(10))
                ));
            }

            orders.put(id, order);
        }
    }
}
