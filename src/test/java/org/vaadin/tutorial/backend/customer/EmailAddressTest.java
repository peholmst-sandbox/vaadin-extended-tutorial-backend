package org.vaadin.tutorial.backend.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@domain.org",
            "user+tag@example.co.uk",
            "firstname.lastname@company.com",
            "email@subdomain.domain.com"
    })
    void constructor_acceptsValidEmails(String email) {
        var emailAddress = new EmailAddress(email);
        assertEquals(email, emailAddress.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "missing@domain",
            "@nodomain.com",
            "spaces in@email.com",
            "no@@double.com",
            ""
    })
    void constructor_rejectsInvalidEmails(String email) {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(email));
    }

    @Test
    void constructor_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
    }

    @Test
    void constructor_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("   "));
    }

    @Test
    void toString_returnsValue() {
        var email = new EmailAddress("test@example.com");
        assertEquals("test@example.com", email.toString());
    }

    @Test
    void equals_worksCorrectly() {
        var email1 = new EmailAddress("test@example.com");
        var email2 = new EmailAddress("test@example.com");
        var email3 = new EmailAddress("other@example.com");

        assertEquals(email1, email2);
        assertNotEquals(email1, email3);
    }
}
