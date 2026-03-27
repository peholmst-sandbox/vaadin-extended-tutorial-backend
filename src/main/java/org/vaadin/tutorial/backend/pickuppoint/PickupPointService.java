package org.vaadin.tutorial.backend.pickuppoint;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.common.TutorialBackendService;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.ValidationException;
import org.vaadin.tutorial.backend.validation.ValidationGroups.OnSave;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class PickupPointService extends TutorialBackendService {

    private static final String[] PICKUP_POINT_NAMES = {
            "Central Station", "Airport Terminal", "Shopping Mall", "City Center",
            "Bus Station", "Train Depot", "Post Office", "Supermarket",
            "Gas Station", "Parking Garage", "Library", "Community Center"
    };

    private static final String[][] CITIES_BY_COUNTRY = {
            {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix"},
            {"London", "Manchester", "Birmingham", "Leeds", "Glasgow"},
            {"Berlin", "Munich", "Hamburg", "Frankfurt", "Cologne"},
            {"Paris", "Marseille", "Lyon", "Toulouse", "Nice"},
            {"Stockholm", "Gothenburg", "Malmö", "Uppsala", "Västerås"}
    };

    private static final String[] COUNTRIES = {
            "USA", "United Kingdom", "Germany", "France", "Sweden"
    };

    // Approximate coordinates for each country (base coordinates)
    private static final double[][] COUNTRY_COORDS = {
            {40.7128, -74.0060},  // USA (New York area)
            {51.5074, -0.1278},   // UK (London area)
            {52.5200, 13.4050},   // Germany (Berlin area)
            {48.8566, 2.3522},    // France (Paris area)
            {59.3293, 18.0686}    // Sweden (Stockholm area)
    };

    private final ConcurrentHashMap<PickupPointId, PickupPointDetails> pickupPoints = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Validator validator;

    public PickupPointService(@Value("${tutorial.backend.artificial-delay:PT0.2S}") Duration artificialDelay) {
        super(artificialDelay);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        generateTestData();
    }

    public Stream<PickupPointDetails> findAll(Query<PickupPointDetails, PickupPointFilter> query) {
        simulateDelay();
        return filteredStream(query.getFilter().orElse(null))
                .sorted(buildComparator(query.getSortOrders()))
                .skip(query.getOffset())
                .limit(query.getLimit())
                .map(PickupPointDetails::new);
    }

    public Stream<PickupPointDetails> findAllBySearchTerm(Query<PickupPointDetails, String> query) {
        return findAll(new Query<>(query.getOffset(), query.getLimit(), query.getSortOrders(), query.getInMemorySorting(), new PickupPointFilter(query.getFilter().orElse(null))));
    }

    public int count(Query<PickupPointDetails, PickupPointFilter> query) {
        simulateDelay();
        return (int) filteredStream(query.getFilter().orElse(null)).count();
    }

    public Optional<PickupPointDetails> findById(PickupPointId id) {
        simulateDelay();
        return Optional.ofNullable(pickupPoints.get(id)).map(PickupPointDetails::new);
    }

    public PickupPointDetails save(PickupPointDetails pickupPointDetails) {
        simulateDelay();
        validate(pickupPointDetails);
        if (pickupPointDetails.getPickupPointId() == null) {
            return insert(pickupPointDetails);
        } else {
            return update(pickupPointDetails);
        }
    }

    private void validate(PickupPointDetails pickupPointDetails) {
        var violations = validator.validate(pickupPointDetails, OnSave.class);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private PickupPointDetails insert(PickupPointDetails pickupPointDetails) {
        var id = new PickupPointId(nextId.getAndIncrement());
        var saved = new PickupPointDetails(pickupPointDetails);
        saved.setPickupPointId(id);
        saved.setVersion(1L);
        pickupPoints.put(id, saved);
        return new PickupPointDetails(saved);
    }

    private PickupPointDetails update(PickupPointDetails pickupPointDetails) {
        assert pickupPointDetails.getPickupPointId() != null;
        var result = pickupPoints.compute(pickupPointDetails.getPickupPointId(), (id, existing) -> {
            if (existing == null) {
                throw new NoSuchElementException("Pickup point not found: " + id);
            }
            if (!Objects.equals(existing.getVersion(), pickupPointDetails.getVersion())) {
                throw new OptimisticLockingFailureException();
            }
            var updated = new PickupPointDetails(pickupPointDetails);
            updated.setPickupPointId(id);
            updated.setVersion(existing.getVersion() + 1);
            return updated;
        });
        return new PickupPointDetails(result);
    }

    private Stream<PickupPointDetails> filteredStream(@Nullable PickupPointFilter filter) {
        var stream = pickupPoints.values().stream();
        if (filter != null && filter.searchTerm() != null && !filter.searchTerm().isBlank()) {
            var term = filter.searchTerm().toLowerCase(Locale.ROOT);
            stream = stream.filter(p ->
                    contains(p.getName(), term)
                            || contains(p.getCity(), term)
                            || contains(p.getCountry(), term)
            );
        }
        return stream;
    }

    private static boolean contains(@Nullable String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private static Comparator<PickupPointDetails> buildComparator(List<QuerySortOrder> sortOrders) {
        Comparator<PickupPointDetails> comparator = null;
        for (var sortOrder : sortOrders) {
            Comparator<PickupPointDetails> propertyComparator = propertyComparator(PickupPointSortProperty.valueOf(sortOrder.getSorted()));
            if (sortOrder.getDirection() == SortDirection.DESCENDING) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator != null ? comparator : Comparator.comparing(p -> p.getPickupPointId().id());
    }

    @SuppressWarnings("unchecked")
    private static Comparator<PickupPointDetails> propertyComparator(PickupPointSortProperty property) {
        return Comparator.comparing(p -> (Comparable<Object>) getProperty(p, property), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static @Nullable Comparable<?> getProperty(PickupPointDetails pickupPoint, PickupPointSortProperty property) {
        return switch (property) {
            case NAME -> pickupPoint.getName();
            case CITY -> pickupPoint.getCity();
            case COUNTRY -> pickupPoint.getCountry();
            case LATITUDE -> pickupPoint.getCoordinate() != null ? pickupPoint.getCoordinate().latitude() : null;
            case LONGITUDE -> pickupPoint.getCoordinate() != null ? pickupPoint.getCoordinate().longitude() : null;
            case ACTIVE -> pickupPoint.getActive();
            case PICKUP_POINT_ID -> pickupPoint.getPickupPointId() != null ? pickupPoint.getPickupPointId().id() : null;
        };
    }

    private void generateTestData() {
        var random = new Random(456);

        for (int i = 0; i < 50; i++) {
            int countryIndex = random.nextInt(COUNTRIES.length);
            var country = COUNTRIES[countryIndex];
            var cities = CITIES_BY_COUNTRY[countryIndex];
            var city = cities[random.nextInt(cities.length)];
            var name = PICKUP_POINT_NAMES[random.nextInt(PICKUP_POINT_NAMES.length)];
            var baseCoords = COUNTRY_COORDS[countryIndex];

            var pickupPoint = new PickupPointDetails();
            var id = new PickupPointId(nextId.getAndIncrement());
            pickupPoint.setPickupPointId(id);
            pickupPoint.setVersion(1L);
            pickupPoint.setName(name + " " + (i + 1));
            pickupPoint.setCity(city);
            pickupPoint.setCountry(country);
            pickupPoint.setCoordinate(GeoCoordinate.of(
                    baseCoords[0] + (random.nextDouble() - 0.5) * 2,
                    baseCoords[1] + (random.nextDouble() - 0.5) * 2
            ));
            pickupPoint.setActive(random.nextInt(10) > 1); // 90% active

            pickupPoints.put(id, pickupPoint);
        }
    }
}
