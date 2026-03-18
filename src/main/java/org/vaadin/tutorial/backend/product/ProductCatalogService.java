package org.vaadin.tutorial.backend.product;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.data.DataIntegrityViolationException;
import org.vaadin.tutorial.backend.data.OptimisticLockingFailureException;
import org.vaadin.tutorial.backend.data.Query;
import org.vaadin.tutorial.backend.data.SortOrder;
import org.vaadin.tutorial.backend.financial.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class ProductCatalogService {

    private static final String[] CATEGORIES = {
            "Electronics", "Clothing", "Home & Garden", "Sports & Outdoors",
            "Books", "Toys & Games", "Food & Beverages", "Health & Beauty",
            "Automotive", "Office Supplies"
    };

    private static final String[][] BRANDS = {
            {"Samsung", "Apple", "Sony", "LG", "Philips"},
            {"Nike", "Adidas", "Levi's", "H&M", "Zara"},
            {"IKEA", "KitchenAid", "Dyson", "Bosch", "Gardena"},
            {"Nike", "Adidas", "Puma", "Under Armour", "Wilson"},
            {"Penguin", "HarperCollins", "O'Reilly", "Wiley", "Springer"},
            {"LEGO", "Hasbro", "Mattel", "Ravensburger", "Playmobil"},
            {"Nestlé", "Barilla", "Kellogg's", "Lavazza", "Lindt"},
            {"Nivea", "L'Oréal", "Dove", "Oral-B", "Braun"},
            {"Bosch", "Michelin", "Castrol", "Thule", "Garmin"},
            {"Staedtler", "Moleskine", "Logitech", "HP", "3M"}
    };

    private static final String[][] PRODUCT_TYPES = {
            {"Headphones", "Laptop", "Tablet", "Speaker", "Monitor", "Keyboard", "Camera", "Smartwatch", "Router", "Hard Drive"},
            {"T-Shirt", "Jeans", "Jacket", "Sneakers", "Hoodie", "Dress", "Shorts", "Sweater", "Coat", "Polo Shirt"},
            {"Blender", "Vacuum Cleaner", "Lamp", "Shelf", "Lawn Mower", "Toaster", "Curtains", "Rug", "Plant Pot", "Tool Set"},
            {"Running Shoes", "Yoga Mat", "Tennis Racket", "Bicycle", "Dumbbells", "Football", "Backpack", "Swim Goggles", "Ski Poles", "Jump Rope"},
            {"Novel", "Cookbook", "Textbook", "Biography", "Travel Guide", "Art Book", "Dictionary", "Anthology", "Manual", "Journal"},
            {"Building Set", "Board Game", "Puzzle", "Action Figure", "Doll", "Card Game", "Remote Control Car", "Plush Toy", "Science Kit", "Train Set"},
            {"Pasta", "Coffee Beans", "Chocolate", "Cereal", "Olive Oil", "Tea", "Honey", "Spice Set", "Dried Fruit", "Energy Bar"},
            {"Shampoo", "Face Cream", "Toothbrush", "Perfume", "Sunscreen", "Hair Dryer", "Vitamins", "Body Lotion", "Lip Balm", "Razor"},
            {"Car Battery", "Tire", "Engine Oil", "Roof Rack", "GPS Navigator", "Dash Cam", "Car Seat", "Floor Mats", "Air Freshener", "Jump Starter"},
            {"Pen Set", "Notebook", "Desk Lamp", "Wireless Mouse", "Printer Paper", "Tape Dispenser", "Stapler", "Whiteboard", "Calendar", "File Folder"}
    };

    private static final String[] ADJECTIVES = {
            "Premium", "Classic", "Modern", "Essential", "Professional",
            "Deluxe", "Compact", "Advanced", "Ultra", "Eco-Friendly",
            "Lightweight", "Heavy-Duty", "Vintage", "Signature", "Original"
    };

    private static final int[][] PRICE_RANGE = {
            {2000, 80000},   // Electronics
            {1500, 15000},   // Clothing
            {1000, 50000},   // Home & Garden
            {1000, 40000},   // Sports & Outdoors
            {500, 5000},     // Books
            {800, 15000},    // Toys & Games
            {200, 4000},     // Food & Beverages
            {300, 8000},     // Health & Beauty
            {1000, 60000},   // Automotive
            {200, 10000}     // Office Supplies
    };

    private final ConcurrentHashMap<ProductId, ProductDetails> products = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private volatile Duration artificialDelay = Duration.ofMillis(200);

    public ProductCatalogService() {
        generateTestData();
    }

    public void setArtificialDelay(Duration delay) {
        this.artificialDelay = Objects.requireNonNull(delay);
    }

    public Duration getArtificialDelay() {
        return artificialDelay;
    }

    public List<ProductCatalogItem> findItems(Query<ProductFilter> query) {
        simulateDelay();
        return filteredStream(query.filter())
                .sorted(buildComparator(query.sortOrders()))
                .skip(query.offset())
                .limit(query.limit())
                .map(this::toCatalogItem)
                .toList();
    }

    public int countItems(Query<ProductFilter> query) {
        simulateDelay();
        return (int) filteredStream(query.filter()).count();
    }

    public Optional<ProductDetails> findDetailsById(ProductId id) {
        simulateDelay();
        return Optional.ofNullable(products.get(id)).map(ProductDetails::new);
    }

    public Optional<ProductCatalogItem> findItemById(ProductId id) {
        simulateDelay();
        return Optional.ofNullable(products.get(id)).map(this::toCatalogItem);
    }

    public ProductDetails save(ProductDetails productDetails) {
        simulateDelay();
        if (productDetails.getProductId() == null) {
            return insert(productDetails);
        } else {
            return update(productDetails);
        }
    }

    private ProductDetails insert(ProductDetails productDetails) {
        checkForDuplicateSku(productDetails.getSku(), null);
        var id = new ProductId(nextId.getAndIncrement());
        var saved = new ProductDetails(productDetails);
        saved.setProductId(id);
        saved.setVersion(1L);
        products.put(id, saved);
        return new ProductDetails(saved);
    }

    private ProductDetails update(ProductDetails productDetails) {
        checkForDuplicateSku(productDetails.getSku(), productDetails.getProductId());
        var result = products.compute(productDetails.getProductId(), (id, existing) -> {
            if (existing == null) {
                throw new NoSuchElementException("Product not found: " + id);
            }
            if (!Objects.equals(existing.getVersion(), productDetails.getVersion())) {
                throw new OptimisticLockingFailureException();
            }
            var updated = new ProductDetails(productDetails);
            updated.setProductId(id);
            updated.setVersion(existing.getVersion() + 1);
            return updated;
        });
        return new ProductDetails(result);
    }

    private void checkForDuplicateSku(@Nullable String sku, @Nullable ProductId excludeId) {
        if (sku == null) {
            return;
        }
        boolean duplicateExists = products.values().stream()
                .anyMatch(p -> sku.equals(p.getSku()) && !p.getProductId().equals(excludeId));
        if (duplicateExists) {
            throw new DataIntegrityViolationException("SKU already exists: " + sku);
        }
    }

    private Stream<ProductDetails> filteredStream(ProductFilter filter) {
        var stream = products.values().stream();
        if (filter != null && filter.searchTerm() != null && !filter.searchTerm().isBlank()) {
            var term = filter.searchTerm().toLowerCase(Locale.ROOT);
            stream = stream.filter(p ->
                    contains(p.getName(), term)
                            || contains(p.getDescription(), term)
                            || contains(p.getCategory(), term)
                            || contains(p.getBrand(), term)
                            || contains(p.getSku(), term)
            );
        }
        return stream;
    }

    private static boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private static Comparator<ProductDetails> buildComparator(List<SortOrder> sortOrders) {
        Comparator<ProductDetails> comparator = null;
        for (var sortOrder : sortOrders) {
            for (var property : sortOrder.properties()) {
                Comparator<ProductDetails> propertyComparator = propertyComparator(property);
                if (sortOrder.isDescending()) {
                    propertyComparator = propertyComparator.reversed();
                }
                comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
            }
        }
        return comparator != null ? comparator : Comparator.comparing(p -> p.getProductId().id());
    }

    @SuppressWarnings("unchecked")
    private static Comparator<ProductDetails> propertyComparator(String property) {
        return Comparator.comparing(p -> (Comparable<Object>) getProperty(p, property), Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static Comparable<?> getProperty(ProductDetails product, String property) {
        return switch (property) {
            case "name" -> product.getName();
            case "description" -> product.getDescription();
            case "category" -> product.getCategory();
            case "brand" -> product.getBrand();
            case "sku" -> product.getSku();
            case "releaseDate" -> product.getReleaseDate();
            case "price" -> product.getPrice() != null ? product.getPrice().amount() : null;
            case "discount" -> product.getDiscount() != null ? product.getDiscount().amount() : null;
            case "productId" -> product.getProductId() != null ? product.getProductId().id() : null;
            default -> throw new IllegalArgumentException("Unknown sort property: " + property);
        };
    }

    private ProductCatalogItem toCatalogItem(ProductDetails details) {
        return new ProductCatalogItem(
                details.getProductId(),
                details.getName(),
                details.getDescription(),
                details.getCategory(),
                details.getBrand(),
                details.getPrice()
        );
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
        var random = new Random(42);
        var baseDate = LocalDate.of(2024, 1, 1);

        for (int i = 0; i < 300; i++) {
            int categoryIndex = i % CATEGORIES.length;
            var category = CATEGORIES[categoryIndex];
            var brands = BRANDS[categoryIndex];
            var productTypes = PRODUCT_TYPES[categoryIndex];
            var priceRange = PRICE_RANGE[categoryIndex];

            var brand = brands[random.nextInt(brands.length)];
            var productType = productTypes[i % productTypes.length];
            var adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];

            var product = new ProductDetails();
            var id = new ProductId(nextId.getAndIncrement());
            product.setProductId(id);
            product.setVersion(1L);
            product.setName(adjective + " " + productType);
            product.setDescription(brand + " " + adjective.toLowerCase(Locale.ROOT) + " " + productType.toLowerCase(Locale.ROOT)
                    + " - high quality " + category.toLowerCase(Locale.ROOT) + " product.");
            product.setCategory(category);
            product.setBrand(brand);
            product.setSku("SKU-" + String.format("%04d", id.id()));
            product.setReleaseDate(baseDate.plusDays(random.nextInt(730)));

            int priceInCents = priceRange[0] + random.nextInt(priceRange[1] - priceRange[0]);
            product.setPrice(new Money(BigDecimal.valueOf(priceInCents, 2)));

            if (random.nextInt(4) == 0) {
                int discountInCents = priceInCents / (5 + random.nextInt(6));
                product.setDiscount(new Money(BigDecimal.valueOf(discountInCents, 2)));
            }

            products.put(id, product);
        }
    }
}
