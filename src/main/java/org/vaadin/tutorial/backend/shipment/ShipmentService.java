package org.vaadin.tutorial.backend.shipment;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.order.OrderDetails;
import org.vaadin.tutorial.backend.order.OrderId;
import org.vaadin.tutorial.backend.order.OrderItem;
import org.vaadin.tutorial.backend.product.ProductId;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class ShipmentService {

    private final ConcurrentHashMap<ShipmentId, ShipmentDetails> shipments = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Validator validator;
    private final Duration artificialDelay;

    public ShipmentService(@Value("${tutorial.backend.artificial-delay:PT0.2S}") Duration artificialDelay) {
        this.artificialDelay = artificialDelay;
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    public List<ShipmentDetails> findAll(Query<ShipmentFilter, ShipmentSortProperty> query) {
        simulateDelay();
        return filteredStream(query.filter())
                .sorted(buildComparator(query.sortOrders()))
                .skip(query.offset())
                .limit(query.limit())
                .map(ShipmentDetails::new)
                .toList();
    }

    public int count(Query<ShipmentFilter, ShipmentSortProperty> query) {
        simulateDelay();
        return (int) filteredStream(query.filter()).count();
    }

    public Optional<ShipmentDetails> findById(ShipmentId id) {
        simulateDelay();
        return Optional.ofNullable(shipments.get(id)).map(ShipmentDetails::new);
    }

    /**
     * Finds all shipments for a given order.
     *
     * @param orderId the order ID
     * @return list of shipments for the order
     */
    public List<ShipmentDetails> findByOrderId(OrderId orderId) {
        simulateDelay();
        return shipments.values().stream()
                .filter(s -> orderId.equals(s.getOrderId()))
                .map(ShipmentDetails::new)
                .toList();
    }

    /**
     * Calculates the total shipped quantity for each product in an order.
     *
     * @param orderId the order ID
     * @return map of product ID to total shipped quantity
     */
    public Map<ProductId, Integer> getShippedQuantities(OrderId orderId) {
        simulateDelay();
        var quantities = new HashMap<ProductId, Integer>();
        shipments.values().stream()
                .filter(s -> orderId.equals(s.getOrderId()))
                .flatMap(s -> s.getItems().stream())
                .forEach(item -> quantities.merge(item.productId(), item.quantity().value(), Integer::sum));
        return quantities;
    }

    /**
     * Calculates the remaining (unshipped) items for an order.
     *
     * @param order the order details
     * @return list of items with remaining quantities (excludes fully shipped items)
     */
    public List<ShipmentItem> getRemainingItems(OrderDetails order) {
        simulateDelay();
        assert order.getOrderId() != null;
        var shipped = getShippedQuantitiesInternal(order.getOrderId());
        var remaining = new ArrayList<ShipmentItem>();

        for (OrderItem orderItem : order.getItems()) {
            int orderedQty = orderItem.quantity().value();
            int shippedQty = shipped.getOrDefault(orderItem.productId(), 0);
            int remainingQty = orderedQty - shippedQty;
            if (remainingQty > 0) {
                remaining.add(new ShipmentItem(orderItem.productId(), new Quantity(remainingQty)));
            }
        }
        return remaining;
    }

    /**
     * Checks if all items in an order have been fully shipped.
     *
     * @param order the order details
     * @return true if all items are fully shipped
     */
    public boolean isFullyShipped(OrderDetails order) {
        return getRemainingItems(order).isEmpty();
    }

    public ShipmentDetails save(ShipmentDetails shipmentDetails) {
        simulateDelay();
        validate(shipmentDetails);
        if (shipmentDetails.getShipmentId() == null) {
            return insert(shipmentDetails);
        } else {
            return update(shipmentDetails);
        }
    }

    private void validate(ShipmentDetails shipmentDetails) {
        var violations = validator.validate(shipmentDetails, OnSave.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private ShipmentDetails insert(ShipmentDetails shipmentDetails) {
        var id = new ShipmentId(nextId.getAndIncrement());
        var saved = new ShipmentDetails(shipmentDetails);
        saved.setShipmentId(id);
        saved.setVersion(1L);
        shipments.put(id, saved);
        return new ShipmentDetails(saved);
    }

    private ShipmentDetails update(ShipmentDetails shipmentDetails) {
        assert shipmentDetails.getShipmentId() != null;
        var result = shipments.compute(shipmentDetails.getShipmentId(), (id, existing) -> {
            if (existing == null) {
                throw new NoSuchElementException("Shipment not found: " + id);
            }
            if (!Objects.equals(existing.getVersion(), shipmentDetails.getVersion())) {
                throw new OptimisticLockingFailureException();
            }
            var updated = new ShipmentDetails(shipmentDetails);
            updated.setShipmentId(id);
            updated.setVersion(existing.getVersion() + 1);
            return updated;
        });
        return new ShipmentDetails(result);
    }

    // Internal version without delay for use by other methods
    private Map<ProductId, Integer> getShippedQuantitiesInternal(OrderId orderId) {
        var quantities = new HashMap<ProductId, Integer>();
        shipments.values().stream()
                .filter(s -> orderId.equals(s.getOrderId()))
                .flatMap(s -> s.getItems().stream())
                .forEach(item -> quantities.merge(item.productId(), item.quantity().value(), Integer::sum));
        return quantities;
    }

    private Stream<ShipmentDetails> filteredStream(@Nullable ShipmentFilter filter) {
        var stream = shipments.values().stream();
        if (filter != null) {
            if (filter.orderId() != null) {
                stream = stream.filter(s -> filter.orderId().equals(s.getOrderId()));
            }
            if (filter.state() != null) {
                stream = stream.filter(s -> filter.state().equals(s.getState()));
            }
        }
        return stream;
    }

    private static Comparator<ShipmentDetails> buildComparator(List<SortOrder<ShipmentSortProperty>> sortOrders) {
        Comparator<ShipmentDetails> comparator = null;
        for (var sortOrder : sortOrders) {
            Comparator<ShipmentDetails> propertyComparator = propertyComparator(sortOrder.property());
            if (sortOrder.direction() == SortOrder.Direction.DESCENDING) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(s -> s.getShipmentId().id());
    }

    @SuppressWarnings("unchecked")
    private static Comparator<ShipmentDetails> propertyComparator(ShipmentSortProperty property) {
        return Comparator.comparing(s -> (Comparable<Object>) getProperty(s, property), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparable<?> getProperty(ShipmentDetails shipment, ShipmentSortProperty property) {
        return switch (property) {
            case SHIPMENT_ID -> shipment.getShipmentId() != null ? shipment.getShipmentId().id() : null;
            case ORDER_ID -> shipment.getOrderId() != null ? shipment.getOrderId().id() : null;
            case STATE -> shipment.getState() != null ? shipment.getState().ordinal() : null;
            case ITEM_COUNT -> shipment.getItems().size();
        };
    }

    private void simulateDelay() {
        var delay = artificialDelay;
        if (!delay.isZero() && !delay.isNegative()) {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during simulated delay", e);
            }
        }
    }
}
