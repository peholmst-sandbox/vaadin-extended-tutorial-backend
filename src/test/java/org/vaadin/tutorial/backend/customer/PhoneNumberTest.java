package org.vaadin.tutorial.backend.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "+1 555-123-4567",
            "(555) 123-4567",
            "555.123.4567",
            "+46 70 123 45 67",
            "1234567890",
            "+44 20 7946 0958"
    })
    void constructor_acceptsValidPhoneNumbers(String phone) {
        var phoneNumber = new PhoneNumber(phone);
        assertEquals(phone, phoneNumber.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123",
            "abc-def-ghij",
            "++1234567890",
            ""
    })
    void constructor_rejectsInvalidPhoneNumbers(String phone) {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(phone));
    }

    @Test
    void constructor_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(null));
    }

    @Test
    void constructor_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber("   "));
    }

    @Test
    void toString_returnsValue() {
        var phone = new PhoneNumber("+1 555-123-4567");
        assertEquals("+1 555-123-4567", phone.toString());
    }

    @Test
    void equals_worksCorrectly() {
        var phone1 = new PhoneNumber("+1 555-123-4567");
        var phone2 = new PhoneNumber("+1 555-123-4567");
        var phone3 = new PhoneNumber("+1 555-999-8888");

        assertEquals(phone1, phone2);
        assertNotEquals(phone1, phone3);
    }
}
