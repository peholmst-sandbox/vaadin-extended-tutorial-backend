package org.vaadin.tutorial.backend.pickuppoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GeoCoordinateTest {

    @Test
    void constructor_acceptsValidCoordinates() {
        var coord = new GeoCoordinate(new BigDecimal("59.3293"), new BigDecimal("18.0686"));

        assertEquals(new BigDecimal("59.3293000"), coord.latitude());
        assertEquals(new BigDecimal("18.0686000"), coord.longitude());
    }

    @Test
    void of_createsFromDoubles() {
        var coord = GeoCoordinate.of(59.3293, 18.0686);

        assertEquals(0, coord.latitude().compareTo(new BigDecimal("59.3293")));
        assertEquals(0, coord.longitude().compareTo(new BigDecimal("18.0686")));
    }

    @Test
    void constructor_roundsToSevenDecimalPlaces() {
        var coord = new GeoCoordinate(
                new BigDecimal("59.329312345678"),
                new BigDecimal("18.068612345678")
        );

        assertEquals(new BigDecimal("59.3293123"), coord.latitude());
        assertEquals(new BigDecimal("18.0686123"), coord.longitude());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "90, 180",
            "-90, -180",
            "45.5, -122.6",
            "-33.8688, 151.2093"
    })
    void constructor_acceptsValidRanges(String lat, String lon) {
        var coord = new GeoCoordinate(new BigDecimal(lat), new BigDecimal(lon));
        assertNotNull(coord);
    }

    @Test
    void constructor_rejectsLatitudeTooHigh() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(new BigDecimal("90.0001"), BigDecimal.ZERO));
    }

    @Test
    void constructor_rejectsLatitudeTooLow() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(new BigDecimal("-90.0001"), BigDecimal.ZERO));
    }

    @Test
    void constructor_rejectsLongitudeTooHigh() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(BigDecimal.ZERO, new BigDecimal("180.0001")));
    }

    @Test
    void constructor_rejectsLongitudeTooLow() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(BigDecimal.ZERO, new BigDecimal("-180.0001")));
    }

    @Test
    void constructor_rejectsNullLatitude() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(null, BigDecimal.ZERO));
    }

    @Test
    void constructor_rejectsNullLongitude() {
        assertThrows(IllegalArgumentException.class, () ->
                new GeoCoordinate(BigDecimal.ZERO, null));
    }

    @Test
    void toString_formatsNicely() {
        var coord = GeoCoordinate.of(59.3293, 18.0686);
        assertEquals("59.3293, 18.0686", coord.toString());
    }

    @Test
    void equals_worksCorrectly() {
        var coord1 = GeoCoordinate.of(59.3293, 18.0686);
        var coord2 = GeoCoordinate.of(59.3293, 18.0686);
        var coord3 = GeoCoordinate.of(60.0, 18.0686);

        assertEquals(coord1, coord2);
        assertNotEquals(coord1, coord3);
    }
}
