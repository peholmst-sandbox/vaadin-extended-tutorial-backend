package org.vaadin.tutorial.backend.customer;

import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.tutorial.backend.common.EmailAddress;
import org.vaadin.tutorial.backend.common.PhoneNumber;
import org.vaadin.tutorial.backend.data.DataIntegrityViolationException;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.ValidationException;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(Duration.ZERO);
    }

    @Test
    void save_insertsNewCustomer() {
        var customer = createCustomer("new.customer@example.com");

        var saved = service.save(customer);

        assertNotNull(saved.getCustomerId());
        assertEquals(1L, saved.getVersion());
        assertEquals("new.customer@example.com", saved.getEmail().value());
    }

    @Test
    void save_updatesExistingCustomer() {
        var customer = createCustomer("update.test@example.com");
        var saved = service.save(customer);

        saved.setFirstName("Updated");
        var updated = service.save(saved);

        assertEquals(saved.getCustomerId(), updated.getCustomerId());
        assertEquals(2L, updated.getVersion());
        assertEquals("Updated", updated.getFirstName());
    }

    @Test
    void save_throwsExceptionForDuplicateEmailOnInsert() {
        var customer1 = createCustomer("duplicate@example.com");
        service.save(customer1);

        var customer2 = createCustomer("duplicate@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> service.save(customer2));
    }

    @Test
    void save_throwsExceptionForDuplicateEmailOnUpdate() {
        var customer1 = createCustomer("first@example.com");
        service.save(customer1);

        var customer2 = createCustomer("second@example.com");
        var saved2 = service.save(customer2);

        saved2.setEmail(new EmailAddress("first@example.com"));

        assertThrows(DataIntegrityViolationException.class, () -> service.save(saved2));
    }

    @Test
    void save_allowsUpdatingCustomerWithSameEmail() {
        var customer = createCustomer("same.email@example.com");
        var saved = service.save(customer);

        saved.setFirstName("Updated");

        var updated = service.save(saved);

        assertEquals("same.email@example.com", updated.getEmail().value());
        assertEquals("Updated", updated.getFirstName());
    }

    @Test
    void save_throwsOptimisticLockingFailureOnVersionMismatch() {
        var customer = createCustomer("opt.lock@example.com");
        var saved = service.save(customer);

        saved.setFirstName("First update");
        service.save(saved);

        saved.setFirstName("Stale update");

        assertThrows(OptimisticLockingFailureException.class, () -> service.save(saved));
    }

    @Test
    void save_throwsNoSuchElementExceptionForNonExistentCustomer() {
        var customer = createCustomer("nonexistent@example.com");
        customer.setCustomerId(new CustomerId(999999L));
        customer.setVersion(1L);

        assertThrows(NoSuchElementException.class, () -> service.save(customer));
    }

    @Test
    void save_throwsValidationExceptionWhenFirstNameIsNull() {
        var customer = createCustomer("valid@example.com");
        customer.setFirstName(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(customer));
        assertTrue(exception.getMessage().contains("firstName"));
    }

    @Test
    void save_throwsValidationExceptionWhenLastNameIsNull() {
        var customer = createCustomer("valid@example.com");
        customer.setLastName(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(customer));
        assertTrue(exception.getMessage().contains("lastName"));
    }

    @Test
    void save_throwsValidationExceptionWhenEmailIsNull() {
        var customer = createCustomer("valid@example.com");
        customer.setEmail(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(customer));
        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    void save_throwsValidationExceptionWhenPhoneIsNull() {
        var customer = createCustomer("valid@example.com");
        customer.setPhone(null);

        var exception = assertThrows(ValidationException.class, () -> service.save(customer));
        assertTrue(exception.getMessage().contains("phone"));
    }

    @Test
    void save_throwsValidationExceptionForBlankFirstName() {
        var customer = createCustomer("valid@example.com");
        customer.setFirstName("   ");

        assertThrows(ValidationException.class, () -> service.save(customer));
    }

    @Test
    void findById_returnsCustomer() {
        var customer = createCustomer("find.me@example.com");
        var saved = service.save(customer);

        var found = service.findById(saved.getCustomerId());

        assertTrue(found.isPresent());
        assertEquals(saved.getCustomerId(), found.get().getCustomerId());
    }

    @Test
    void findById_returnsEmptyForNonExistent() {
        var found = service.findById(new CustomerId(999999L));

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsCustomers() {
        var query = new Query<CustomerDetails, CustomerFilter>(0, 10, List.of(), null, null);

        var customers = service.findAll(query).toList();

        assertFalse(customers.isEmpty());
    }

    @Test
    void count_returnsCount() {
        var query = new Query<CustomerDetails, CustomerFilter>(0, 10, List.of(), null, null);

        var count = service.count(query);

        assertTrue(count > 0);
    }

    private CustomerDetails createCustomer(String email) {
        var customer = new CustomerDetails();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail(new EmailAddress(email));
        customer.setPhone(new PhoneNumber("+1 555-123-4567"));
        return customer;
    }
}
