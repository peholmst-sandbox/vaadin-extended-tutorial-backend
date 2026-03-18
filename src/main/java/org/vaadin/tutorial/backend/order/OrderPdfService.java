package org.vaadin.tutorial.backend.order;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.vaadin.tutorial.backend.customer.CustomerDetails;
import org.vaadin.tutorial.backend.customer.CustomerService;
import org.vaadin.tutorial.backend.financial.Money;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointDetails;
import org.vaadin.tutorial.backend.pickuppoint.PickupPointService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

/**
 * Service for generating PDF documents from orders.
 */
@Service
public class OrderPdfService {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final PickupPointService pickupPointService;

    public OrderPdfService(OrderService orderService,
                           CustomerService customerService,
                           PickupPointService pickupPointService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.pickupPointService = pickupPointService;
    }

    /**
     * Generates a PDF document for the specified order.
     *
     * @param orderId the ID of the order to generate a PDF for
     * @return an InputStream containing the PDF document
     * @throws NoSuchElementException if the order is not found
     */
    public InputStream generatePdf(OrderId orderId) {
        var order = orderService.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        var customer = order.getCustomerId() != null
                ? customerService.findById(order.getCustomerId()).orElse(null)
                : null;

        var pickupPoint = order.getPickupPointId() != null
                ? pickupPointService.findById(order.getPickupPointId()).orElse(null)
                : null;

        var outputStream = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addHeader(document, order);
            addCustomerSection(document, customer);
            addPickupPointSection(document, pickupPoint);
            addItemsTable(document, order);
            addTotals(document, order);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void addHeader(Document document, OrderDetails order) throws DocumentException {
        var title = new Paragraph("Order #" + order.getOrderId().id(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void addCustomerSection(Document document, CustomerDetails customer) throws DocumentException {
        var sectionTitle = new Paragraph("Customer Information",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        if (customer != null) {
            document.add(new Paragraph("Name: " + customer.getFirstName() + " " + customer.getLastName()));
            if (customer.getEmail() != null) {
                document.add(new Paragraph("Email: " + customer.getEmail().value()));
            }
            if (customer.getPhone() != null) {
                document.add(new Paragraph("Phone: " + customer.getPhone().value()));
            }
        } else {
            document.add(new Paragraph("Customer information not available"));
        }

        document.add(Chunk.NEWLINE);
    }

    private void addPickupPointSection(Document document, PickupPointDetails pickupPoint) throws DocumentException {
        var sectionTitle = new Paragraph("Pickup Point",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        if (pickupPoint != null) {
            document.add(new Paragraph("Name: " + pickupPoint.getName()));
            document.add(new Paragraph("Location: " + pickupPoint.getCity() + ", " + pickupPoint.getCountry()));
        } else {
            document.add(new Paragraph("Pickup point information not available"));
        }

        document.add(Chunk.NEWLINE);
    }

    private void addItemsTable(Document document, OrderDetails order) throws DocumentException {
        var sectionTitle = new Paragraph("Order Items",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        var table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1.5f, 1.5f, 1, 1.5f});

        addTableHeader(table, "Product");
        addTableHeader(table, "Unit Price");
        addTableHeader(table, "Discount");
        addTableHeader(table, "Qty");
        addTableHeader(table, "Total");

        for (var item : order.getItems()) {
            table.addCell(item.productName());
            table.addCell(formatMoney(item.unitPrice()));
            table.addCell(item.discount() != null ? formatMoney(item.discount()) : "-");
            table.addCell(String.valueOf(item.quantity().value()));
            table.addCell(formatMoney(calculateLineTotal(item)));
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addTableHeader(PdfPTable table, String text) {
        var cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotals(Document document, OrderDetails order) throws DocumentException {
        var subtotal = BigDecimal.ZERO;
        var totalDiscount = BigDecimal.ZERO;

        for (var item : order.getItems()) {
            subtotal = subtotal.add(item.totalPrice().amount());
            if (item.totalDiscount() != null) {
                totalDiscount = totalDiscount.add(item.totalDiscount().amount());
            }
        }

        var grandTotal = subtotal.subtract(totalDiscount);

        var totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addTotalRow(totalsTable, "Subtotal:", formatMoney(new Money(subtotal)));
        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "Discount:", "-" + formatMoney(new Money(totalDiscount)));
        }
        addTotalRow(totalsTable, "Total:", formatMoney(new Money(grandTotal)), true);

        document.add(totalsTable);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        addTotalRow(table, label, value, false);
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean bold) {
        var font = bold
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)
                : FontFactory.getFont(FontFactory.HELVETICA, 10);

        var labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        var valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private Money calculateLineTotal(OrderItem item) {
        var total = item.totalPrice();
        var discount = item.totalDiscount();
        if (discount != null) {
            return new Money(total.amount().subtract(discount.amount()));
        }
        return total;
    }

    private String formatMoney(Money money) {
        return "$" + money.amount().toPlainString();
    }
}
