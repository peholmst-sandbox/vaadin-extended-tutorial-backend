package org.vaadin.tutorial.backend.customer;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.data.DataIntegrityViolationException;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class CustomerService {

    private static final String[] FIRST_NAMES = {
            "Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry",
            "Ivy", "Jack", "Kate", "Liam", "Mia", "Noah", "Olivia", "Peter",
            "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier"
    };

    private static final String[] LAST_NAMES = {
            "Anderson", "Brown", "Clark", "Davis", "Evans", "Fisher", "Garcia",
            "Harris", "Ingram", "Johnson", "King", "Lee", "Miller", "Nelson",
            "O'Brien", "Parker", "Quinn", "Roberts", "Smith", "Taylor", "Underwood"
    };

    private static final String[] EMAIL_DOMAINS = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "company.com"
    };

    private final ConcurrentHashMap<CustomerId, CustomerDetails> customers = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Validator validator;
    private volatile Duration artificialDelay = Duration.ofMillis(200);

    public CustomerService() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        generateTestData();
    }

    public void setArtificialDelay(Duration delay) {
        this.artificialDelay = Objects.requireNonNull(delay);
    }

    public Duration getArtificialDelay() {
        return artificialDelay;
    }

    public List<CustomerDetails> findAll(Query<CustomerFilter, CustomerSortProperty> query) {
        simulateDelay();
        return filteredStream(query.filter())
                .sorted(buildComparator(query.sortOrders()))
                .skip(query.offset())
                .limit(query.limit())
                .map(CustomerDetails::new)
                .toList();
    }

    public int count(Query<CustomerFilter, CustomerSortProperty> query) {
        simulateDelay();
        return (int) filteredStream(query.filter()).count();
    }

    public Optional<CustomerDetails> findById(CustomerId id) {
        simulateDelay();
        return Optional.ofNullable(customers.get(id)).map(CustomerDetails::new);
    }

    public CustomerDetails save(CustomerDetails customerDetails) {
        simulateDelay();
        validate(customerDetails);
        if (customerDetails.getCustomerId() == null) {
            return insert(customerDetails);
        } else {
            return update(customerDetails);
        }
    }

    private void validate(CustomerDetails customerDetails) {
        var violations = validator.validate(customerDetails, OnSave.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private CustomerDetails insert(CustomerDetails customerDetails) {
        checkForDuplicateEmail(customerDetails.getEmail(), null);
        var id = new CustomerId(nextId.getAndIncrement());
        var saved = new CustomerDetails(customerDetails);
        saved.setCustomerId(id);
        saved.setVersion(1L);
        customers.put(id, saved);
        return new CustomerDetails(saved);
    }

    private CustomerDetails update(CustomerDetails customerDetails) {
        checkForDuplicateEmail(customerDetails.getEmail(), customerDetails.getCustomerId());
        assert customerDetails.getCustomerId() != null;
        var result = customers.compute(customerDetails.getCustomerId(), (id, existing) -> {
            if (existing == null) {
                throw new NoSuchElementException("Customer not found: " + id);
            }
            if (!Objects.equals(existing.getVersion(), customerDetails.getVersion())) {
                throw new OptimisticLockingFailureException();
            }
            var updated = new CustomerDetails(customerDetails);
            updated.setCustomerId(id);
            updated.setVersion(existing.getVersion() + 1);
            return updated;
        });
        return new CustomerDetails(result);
    }

    private void checkForDuplicateEmail(@Nullable EmailAddress email, @Nullable CustomerId excludeId) {
        if (email == null) {
            return;
        }
        boolean duplicateExists = customers.values().stream()
                .anyMatch(c -> email.equals(c.getEmail()) && !c.getCustomerId().equals(excludeId));
        if (duplicateExists) {
            throw new DataIntegrityViolationException("Email already exists: " + email);
        }
    }

    private Stream<CustomerDetails> filteredStream(@Nullable CustomerFilter filter) {
        var stream = customers.values().stream();
        if (filter != null && filter.searchTerm() != null && !filter.searchTerm().isBlank()) {
            var term = filter.searchTerm().toLowerCase(Locale.ROOT);
            stream = stream.filter(c ->
                    contains(c.getFirstName(), term)
                            || contains(c.getLastName(), term)
                            || contains(c.getEmail() != null ? c.getEmail().value() : null, term)
                            || contains(c.getPhone() != null ? c.getPhone().value() : null, term)
            );
        }
        return stream;
    }

    private static boolean contains(@Nullable String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private static Comparator<CustomerDetails> buildComparator(List<SortOrder<CustomerSortProperty>> sortOrders) {
        Comparator<CustomerDetails> comparator = null;
        for (var sortOrder : sortOrders) {
            Comparator<CustomerDetails> propertyComparator = propertyComparator(sortOrder.property());
            if (sortOrder.direction() == SortOrder.Direction.DESCENDING) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(c -> c.getCustomerId().id());
    }

    @SuppressWarnings("unchecked")
    private static Comparator<CustomerDetails> propertyComparator(CustomerSortProperty property) {
        return Comparator.comparing(c -> (Comparable<Object>) getProperty(c, property), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparable<?> getProperty(CustomerDetails customer, CustomerSortProperty property) {
        return switch (property) {
            case FIRST_NAME -> customer.getFirstName();
            case LAST_NAME -> customer.getLastName();
            case EMAIL -> customer.getEmail() != null ? customer.getEmail().value() : null;
            case PHONE -> customer.getPhone() != null ? customer.getPhone().value() : null;
            case CUSTOMER_ID -> customer.getCustomerId() != null ? customer.getCustomerId().id() : null;
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

    private void generateTestData() {
        var random = new Random(123);

        for (int i = 0; i < 100; i++) {
            var firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            var lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            var domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];

            var customer = new CustomerDetails();
            var id = new CustomerId(nextId.getAndIncrement());
            customer.setCustomerId(id);
            customer.setVersion(1L);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setEmail(new EmailAddress(
                    firstName.toLowerCase() + "." + lastName.toLowerCase() + id.id() + "@" + domain
            ));
            customer.setPhone(new PhoneNumber(
                    "+1 555-" + String.format("%03d", random.nextInt(1000)) + "-" + String.format("%04d", random.nextInt(10000))
            ));

            customers.put(id, customer);
        }
    }
}
