package org.vaadin.tutorial.backend.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vaadin.tutorial.backend.common.EmailAddress;
import org.vaadin.tutorial.backend.common.PhoneNumber;
import org.vaadin.tutorial.backend.common.Quantity;
import org.vaadin.tutorial.backend.customer.CustomerDetails;
import org.vaadin.tutorial.backend.customer.CustomerService;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.pickuppoint.GeoCoordinate;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointDetails;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointService;
import org.vaadin.tutorial.backend.product.ProductId;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class OrderPdfServiceTest {

    private OrderService orderService;
    private CustomerService customerService;
    private PickupPointService pickupPointService;
    private OrderPdfService pdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        orderService.setArtificialDelay(Duration.ZERO);

        customerService = new CustomerService();
        customerService.setArtificialDelay(Duration.ZERO);

        pickupPointService = new PickupPointService();
        pickupPointService.setArtificialDelay(Duration.ZERO);

        pdfService = new OrderPdfService(orderService, customerService, pickupPointService);
    }

    @Test
    void generatePdf_createsValidPdf() throws IOException {
        // Create customer
        var customer = new CustomerDetails();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail(new EmailAddress("john.doe@example.com"));
        customer.setPhone(new PhoneNumber("+1 555-123-4567"));
        var savedCustomer = customerService.save(customer);

        // Create pickup point
        var pickupPoint = new PickupPointDetails();
        pickupPoint.setName("Central Station Pickup");
        pickupPoint.setCity("Stockholm");
        pickupPoint.setCountry("Sweden");
        pickupPoint.setCoordinate(GeoCoordinate.of(59.3293, 18.0686));
        pickupPoint.setActive(true);
        var savedPickupPoint = pickupPointService.save(pickupPoint);

        // Create order with multiple items
        var order = new OrderDetails();
        order.setCustomerId(savedCustomer.getCustomerId());
        order.setPickupPointId(savedPickupPoint.getPickupPointId());
        order.addItem(new OrderItem(
                new ProductId(1),
                "Wireless Headphones",
                new Money(new BigDecimal("149.99")),
                new Money(new BigDecimal("20.00")),
                new Quantity(2)
        ));
        order.addItem(new OrderItem(
                new ProductId(2),
                "USB-C Cable",
                new Money(new BigDecimal("19.99")),
                null,
                new Quantity(3)
        ));
        order.addItem(new OrderItem(
                new ProductId(3),
                "Phone Case Premium Edition",
                new Money(new BigDecimal("39.99")),
                new Money(new BigDecimal("5.00")),
                new Quantity(1)
        ));
        var savedOrder = orderService.save(order);

        // Generate PDF
        var pdfStream = pdfService.generatePdf(savedOrder.getOrderId());

        // Save to temp file
        var pdfFile = tempDir.resolve("order-" + savedOrder.getOrderId().id() + ".pdf");
        Files.copy(pdfStream, pdfFile);

        // Verify the PDF file was created and has content
        assertTrue(Files.exists(pdfFile));
        assertTrue(Files.size(pdfFile) > 0);

        // Verify it starts with PDF magic bytes
        var bytes = Files.readAllBytes(pdfFile);
        assertEquals('%', bytes[0]);
        assertEquals('P', bytes[1]);
        assertEquals('D', bytes[2]);
        assertEquals('F', bytes[3]);

        // Also save to target directory for manual inspection
        var targetDir = Path.of("target");
        Files.createDirectories(targetDir);
        var inspectionFile = targetDir.resolve("test-order.pdf");
        Files.write(inspectionFile, bytes);
        System.out.println("PDF saved for inspection at: " + inspectionFile.toAbsolutePath());
    }

    @Test
    void generatePdf_throwsExceptionForNonExistentOrder() {
        var nonExistentId = new OrderId(999999L);

        assertThrows(NoSuchElementException.class, () -> pdfService.generatePdf(nonExistentId));
    }
}
