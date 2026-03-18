package org.vaadin.tutorial.backend.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.vaadin.tutorial.backend.common.EmailAddress;
import org.vaadin.tutorial.backend.common.PhoneNumber;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointId;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

/**
 * Represents the full details of a customer.
 * <p>
 * This is a mutable bean that supports two states:
 * <ul>
 *   <li><b>Draft state:</b> When created via the no-arg constructor, all fields are null.
 *       This state is used during form editing.</li>
 *   <li><b>Saved state:</b> After being saved via {@link CustomerService#save},
 *       required fields are guaranteed to be non-null, and {@code customerId} and
 *       {@code version} are assigned by the service.</li>
 * </ul>
 * <p>
 * All fields (firstName, lastName, email, phone) are required before save.
 */
public class CustomerDetails {

    private @Nullable CustomerId customerId;
    private @Nullable Long version;

    @NotBlank(groups = OnSave.class, message = "First name is required")
    private @Nullable String firstName;

    @NotBlank(groups = OnSave.class, message = "Last name is required")
    private @Nullable String lastName;

    @NotNull(groups = OnSave.class, message = "Email is required")
    private @Nullable EmailAddress email;

    @NotNull(groups = OnSave.class, message = "Phone is required")
    private @Nullable PhoneNumber phone;

    private @Nullable PickupPointId preferredPickupPointId;

    public CustomerDetails() {
    }

    public CustomerDetails(CustomerDetails original) {
        this.customerId = original.customerId;
        this.version = original.version;
        this.firstName = original.firstName;
        this.lastName = original.lastName;
        this.email = original.email;
        this.phone = original.phone;
        this.preferredPickupPointId = original.preferredPickupPointId;
    }

    public @Nullable CustomerId getCustomerId() {
        return customerId;
    }

    void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public @Nullable Long getVersion() {
        return version;
    }

    void setVersion(Long version) {
        this.version = version;
    }

    public @Nullable String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nullable String firstName) {
        this.firstName = firstName;
    }

    public @Nullable String getLastName() {
        return lastName;
    }

    public void setLastName(@Nullable String lastName) {
        this.lastName = lastName;
    }

    public @Nullable EmailAddress getEmail() {
        return email;
    }

    public void setEmail(@Nullable EmailAddress email) {
        this.email = email;
    }

    public @Nullable PhoneNumber getPhone() {
        return phone;
    }

    public void setPhone(@Nullable PhoneNumber phone) {
        this.phone = phone;
    }

    public @Nullable PickupPointId getPreferredPickupPointId() {
        return preferredPickupPointId;
    }

    public void setPreferredPickupPointId(@Nullable PickupPointId preferredPickupPointId) {
        this.preferredPickupPointId = preferredPickupPointId;
    }
}
