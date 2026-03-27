package org.vaadin.tutorial.backend.pickuppoint;

import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.ValidationException;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PickupPointServiceTest {

    private PickupPointService service;

    @BeforeEach
    void setUp() {
        service = new PickupPointService(Duration.ZERO);
    }

    @Test
    void save_insertsNewPickupPoint() {
        var pickupPoint = createPickupPoint("Test Location");

        var saved = service.save(pickupPoint);

        assertNotNull(saved.getPickupPointId());
        assertEquals(1L, saved.getVersion());
        assertEquals("Test Location", saved.getName());
    }

    @Test
    void save_updatesExistingPickupPoint() {
        var pickupPoint = createPickupPoint("Original Name");
        var saved = service.save(pickupPoint);

        saved.setName("Updated Name");
        var updated = service.save(saved);

        assertEquals(saved.getPickupPointId(), updated.getPickupPointId());
        assertEquals(2L, updated.getVersion());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void save_throwsOptimisticLockingFailureOnVersionMismatch() {
        var pickupPoint = createPickupPoint("Optimistic Lock Test");
        var saved = service.save(pickupPoint);

        saved.setName("First update");
        service.save(saved);

        saved.setName("Stale update");

        assertThrows(OptimisticLockingFailureException.class, () -> service.save(saved));
    }

    @Test
    void save_throwsNoSuchElementExceptionForNonExistentPickupPoint() {
        var pickupPoint = createPickupPoint("Non-existent");
        pickupPoint.setPickupPointId(new PickupPointId(999999L));
        pickupPoint.setVersion(1L);

        assertThrows(NoSuchElementException.class, () -> service.save(pickupPoint));
    }

    @Test
    void save_throwsValidationExceptionWhenNameIsNull() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setName(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(pickupPoint));
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    void save_throwsValidationExceptionWhenCityIsNull() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setCity(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(pickupPoint));
        assertTrue(exception.getMessage().contains("city"));
    }

    @Test
    void save_throwsValidationExceptionWhenCountryIsNull() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setCountry(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(pickupPoint));
        assertTrue(exception.getMessage().contains("country"));
    }

    @Test
    void save_throwsValidationExceptionWhenCoordinateIsNull() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setCoordinate(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(pickupPoint));
        assertTrue(exception.getMessage().contains("coordinate"));
    }

    @Test
    void save_throwsValidationExceptionWhenActiveIsNull() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setActive(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(pickupPoint));
        assertTrue(exception.getMessage().contains("active"));
    }

    @Test
    void save_throwsValidationExceptionForBlankName() {
        var pickupPoint = createPickupPoint("Valid Name");
        pickupPoint.setName("   ");

        assertThrows(ValidationException.class, () -> service.save(pickupPoint));
    }

    @Test
    void save_acceptsActiveTrue() {
        var pickupPoint = createPickupPoint("Active Location");
        pickupPoint.setActive(true);

        var saved = service.save(pickupPoint);

        assertTrue(saved.getActive());
    }

    @Test
    void save_acceptsActiveFalse() {
        var pickupPoint = createPickupPoint("Inactive Location");
        pickupPoint.setActive(false);

        var saved = service.save(pickupPoint);

        assertFalse(saved.getActive());
    }

    @Test
    void findById_returnsPickupPoint() {
        var pickupPoint = createPickupPoint("Find Me");
        var saved = service.save(pickupPoint);

        var found = service.findById(saved.getPickupPointId());

        assertTrue(found.isPresent());
        assertEquals(saved.getPickupPointId(), found.get().getPickupPointId());
    }

    @Test
    void findById_returnsEmptyForNonExistent() {
        var found = service.findById(new PickupPointId(999999L));

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsPickupPoints() {
        var query = new Query<PickupPointDetails, PickupPointFilter>(0, 10, List.of(), null, null);

        var pickupPoints = service.findAll(query).toList();

        assertFalse(pickupPoints.isEmpty());
    }

    @Test
    void count_returnsCount() {
        var query = new Query<PickupPointDetails, PickupPointFilter>(0, 10, List.of(), null, null);

        var count = service.count(query);

        assertTrue(count > 0);
    }

    @Test
    void save_preservesCoordinatePrecision() {
        var pickupPoint = createPickupPoint("Precision Test");
        pickupPoint.setCoordinate(GeoCoordinate.of(59.3293123, 18.0686456));

        var saved = service.save(pickupPoint);

        assertEquals(0, saved.getCoordinate().latitude().compareTo(new java.math.BigDecimal("59.3293123")));
        assertEquals(0, saved.getCoordinate().longitude().compareTo(new java.math.BigDecimal("18.0686456")));
    }

    private PickupPointDetails createPickupPoint(String name) {
        var pickupPoint = new PickupPointDetails();
        pickupPoint.setName(name);
        pickupPoint.setCity("Stockholm");
        pickupPoint.setCountry("Sweden");
        pickupPoint.setCoordinate(GeoCoordinate.of(59.3293, 18.0686));
        pickupPoint.setActive(true);
        return pickupPoint;
    }
}
