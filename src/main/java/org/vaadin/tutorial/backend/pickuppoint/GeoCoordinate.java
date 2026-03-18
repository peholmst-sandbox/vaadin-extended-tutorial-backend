package org.vaadin.tutorial.backend.pickuppoint;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object representing a geographical coordinate pair (WGS84).
 * <p>
 * Coordinates are stored with 7 decimal places precision, which provides
 * approximately 1.1 cm accuracy at the equator.
 */
public record GeoCoordinate(BigDecimal latitude, BigDecimal longitude) implements Serializable {

    /**
     * Number of decimal places for coordinate precision (7 = ~1.1 cm at equator).
     */
    public static final int SCALE = 7;

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    public GeoCoordinate {
        if (latitude == null) {
            throw new IllegalArgumentException("Latitude cannot be null");
        }
        if (longitude == null) {
            throw new IllegalArgumentException("Longitude cannot be null");
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180: " + longitude);
        }
        latitude = latitude.setScale(SCALE, RoundingMode.HALF_UP);
        longitude = longitude.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Creates a GeoCoordinate from double values.
     *
     * @param latitude  the latitude in degrees (-90 to 90)
     * @param longitude the longitude in degrees (-180 to 180)
     * @return a new GeoCoordinate
     */
    public static GeoCoordinate of(double latitude, double longitude) {
        return new GeoCoordinate(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }

    @Override
    public String toString() {
        return latitude.stripTrailingZeros().toPlainString() + ", " +
               longitude.stripTrailingZeros().toPlainString();
    }
}
