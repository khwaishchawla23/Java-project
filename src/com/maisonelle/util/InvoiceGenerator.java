package com.maisonelle.util;

import com.maisonelle.model.CartItem;
import com.maisonelle.model.Order;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * Generates clean, elegant, human-readable text invoices (.txt)
 * using standard Java File I/O. Strictly no third-party PDF dependencies.
 */
public class InvoiceGenerator {

    private final File invoiceDir;
    private static final DecimalFormat RUPEE_FORMAT = new DecimalFormat("₹#,##0.00");

    public InvoiceGenerator(File invoiceDir) {
        this.invoiceDir = invoiceDir;
        if (!this.invoiceDir.exists()) {
            this.invoiceDir.mkdirs();
        }
    }

    public File generateInvoiceFile(Order order) throws IOException {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        String fileName = "Invoice_" + order.getOrderId() + ".txt";
        File invoiceFile = new File(invoiceDir, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(invoiceFile))) {
            writer.println("================================================================================");
            writer.println("                         M A I S O N E L L E  •  MAISONELLE                     ");
            writer.println("                      A little luxury, thoughtfully curated.                    ");
            writer.println("================================================================================");
            writer.println(" TAX INVOICE & ORDER RECEIPT");
            writer.println("--------------------------------------------------------------------------------");
            writer.println(String.format(" Order ID     : %-30s Date : %s", order.getOrderId(), order.getFormattedDate()));
            writer.println(String.format(" Payment Mode : %-30s Status: %s", order.getPaymentMethod(), order.getStatus()));
            writer.println("--------------------------------------------------------------------------------");
            writer.println(" CUSTOMER & SHIPPING DETAILS");
            writer.println(" Name    : " + order.getCustomerName());
            writer.println(" Email   : " + order.getCustomerEmail());
            writer.println(" Phone   : " + order.getCustomerPhone());
            writer.println(" Address : " + order.getShippingAddress());
            writer.println("================================================================================");
            writer.println(String.format(" %-4s %-38s %-6s %-12s %-12s", "#", "Item Description", "Qty", "Unit Price", "Total"));
            writer.println("--------------------------------------------------------------------------------");

            int index = 1;
            for (CartItem item : order.getItems()) {
                String name = item.getProduct().getName();
                if (name.length() > 36) {
                    name = name.substring(0, 33) + "...";
                }
                String unitPrice = RUPEE_FORMAT.format(item.getProduct().getPrice());
                String lineTotal = RUPEE_FORMAT.format(item.getSubtotal());

                writer.println(String.format(" %-4d %-38s %-6d %-12s %-12s",
                        index++, name, item.getQuantity(), unitPrice, lineTotal));
            }

            writer.println("--------------------------------------------------------------------------------");
            writer.println(String.format(" %-50s : %15s", "Cart Subtotal", RUPEE_FORMAT.format(order.getSubtotal())));

            if (order.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                writer.println(String.format(" %-50s : %15s",
                        "Discount (" + order.getCouponCode() + ")", "- " + RUPEE_FORMAT.format(order.getDiscountAmount())));
            }

            writer.println(String.format(" %-50s : %15s", "Statutory GST (18%)", RUPEE_FORMAT.format(order.getGstAmount())));
            writer.println(String.format(" %-50s : %15s", "Delivery & Logistics Fee",
                    order.getDeliveryFee().compareTo(java.math.BigDecimal.ZERO) == 0 ? "FREE" : RUPEE_FORMAT.format(order.getDeliveryFee())));
            writer.println("================================================================================");
            writer.println(String.format(" %-50s : %15s", "FINAL PAYABLE AMOUNT", RUPEE_FORMAT.format(order.getTotalAmount())));
            writer.println("================================================================================");
            writer.println();
            writer.println(" Thank you for shopping with Maisonelle.");
            writer.println(" For customer support & luxury assistance, contact: support@maisonelle.com");
            writer.println(" Registered Office: 12 Rue de la Paix, Bandra Kurla Complex, Mumbai - 400051");
            writer.println("================================================================================");
        }

        return invoiceFile;
    }

    public static void openInvoiceFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.err.println("Could not open invoice file automatically: " + e.getMessage());
        }
    }
}
