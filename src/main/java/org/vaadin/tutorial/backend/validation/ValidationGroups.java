package org.vaadin.tutorial.backend.validation;

/**
 * Marker interfaces for Bean Validation groups.
 * <p>
 * These groups allow different validation rules to be applied at different
 * lifecycle stages of domain objects.
 */
public final class ValidationGroups {

    private ValidationGroups() {
    }

    /**
     * Validation group for pre-save validation.
     * <p>
     * This group validates all fields that must be present before persisting
     * an entity. Fields that are set by the service layer (like IDs and versions)
     * are not validated in this group.
     */
    public interface OnSave {
    }
}
