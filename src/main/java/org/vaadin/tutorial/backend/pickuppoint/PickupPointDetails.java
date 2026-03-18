package org.vaadin.tutorial.backend.pickuppoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

/**
 * Represents the full details of a pickup point.
 * <p>
 * This is a mutable bean that supports two states:
 * <ul>
 *   <li><b>Draft state:</b> When created via the no-arg constructor, all fields are null/default.
 *       This state is used during form editing.</li>
 *   <li><b>Saved state:</b> After being saved via {@link PickupPointService#save},
 *       required fields are guaranteed to be non-null, and {@code pickupPointId} and
 *       {@code version} are assigned by the service.</li>
 * </ul>
 * <p>
 * All fields (name, city, country, coordinate, active) are required before save.
 */
public class PickupPointDetails {

    private @Nullable PickupPointId pickupPointId;
    private @Nullable Long version;

    @NotBlank(groups = OnSave.class, message = "Name is required")
    private @Nullable String name;

    @NotBlank(groups = OnSave.class, message = "City is required")
    private @Nullable String city;

    @NotBlank(groups = OnSave.class, message = "Country is required")
    private @Nullable String country;

    @NotNull(groups = OnSave.class, message = "Coordinate is required")
    private @Nullable GeoCoordinate coordinate;

    @NotNull(groups = OnSave.class, message = "Active status is required")
    private @Nullable Boolean active;

    public PickupPointDetails() {
    }

    public PickupPointDetails(PickupPointDetails original) {
        this.pickupPointId = original.pickupPointId;
        this.version = original.version;
        this.name = original.name;
        this.city = original.city;
        this.country = original.country;
        this.coordinate = original.coordinate;
        this.active = original.active;
    }

    public @Nullable PickupPointId getPickupPointId() {
        return pickupPointId;
    }

    void setPickupPointId(PickupPointId pickupPointId) {
        this.pickupPointId = pickupPointId;
    }

    public @Nullable Long getVersion() {
        return version;
    }

    void setVersion(Long version) {
        this.version = version;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public @Nullable String getCity() {
        return city;
    }

    public void setCity(@Nullable String city) {
        this.city = city;
    }

    public @Nullable String getCountry() {
        return country;
    }

    public void setCountry(@Nullable String country) {
        this.country = country;
    }

    public @Nullable GeoCoordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(@Nullable GeoCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    public @Nullable Boolean getActive() {
        return active;
    }

    public void setActive(@Nullable Boolean active) {
        this.active = active;
    }
}
