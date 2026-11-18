package com.maisonelle.console;

import com.maisonelle.model.CartItem;
import com.maisonelle.model.Order;
import com.maisonelle.model.Product;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Beautiful ASCII formatting utilities for Maisonelle Terminal Edition.
 * Produces clean tabular layouts, aesthetic borders, and currency strings.
 * Strictly Java 21 Standard JDK Built-ins only.
 */
public class ConsoleFormatter {

    private static final DecimalFormat RUPEE_FORMAT = new DecimalFormat("₹#,##0.00");

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return RUPEE_FORMAT.format(amount);
    }

    public static void printAppBanner() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                                ║");
        System.out.println("║              M   A   I   S   O   N   E   L   L   E                             ║");
        System.out.println("║               A little luxury, thoughtfully curated.                           ║");
        System.out.println("║                                                                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
    }

    public static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
        System.out.println("  " + title.toUpperCase());
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
    }

    public static void printSubHeader(String subtitle) {
        System.out.println("\n--- " + subtitle + " ---");
    }

    public static void printSuccess(String message) {
        System.out.println("\n  ✓ " + message);
    }

    public static void printError(String message) {
        System.out.println("\n  ✗ ERROR: " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n  ⚠ NOTICE: " + message);
    }

    public static void printDivider() {
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
    }

    public static void printDoubleDivider() {
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
    }

    /**
     * Prints a formatted table of products.
     */
    public static void printProductTable(List<Product> products) {
        if (products == null || products.isEmpty()) {
            System.out.println("  (No products found matching your criteria)");
            return;
        }

        printDoubleDivider();
        System.out.printf(" %-5s %-30s %-16s %-12s %-8s %-6s\n",
                "ID", "Product Name", "Category", "Price", "Rating", "Stock");
        printDivider();

        for (Product p : products) {
            String name = p.getName();
            if (name.length() > 28) {
                name = name.substring(0, 25) + "...";
            }
            String category = p.getCategory();
            if (category.length() > 14) {
                category = category.substring(0, 12) + "..";
            }
            String price = formatCurrency(p.getPrice());
            String rating = String.format("★ %.1f", p.getRating());
            String stockStr = p.getStock() > 0 ? String.valueOf(p.getStock()) : "OUT";

            System.out.printf(" %-5d %-30s %-16s %-12s %-8s %-6s\n",
                    p.getId(), name, category, price, rating, stockStr);
        }
        printDoubleDivider();
        System.out.println(" Total Products Listed: " + products.size());
    }

    /**
     * Prints a detailed single product card.
     */
    public static void printProductCard(Product p) {
        if (p == null) return;

        printDoubleDivider();
        System.out.printf("  PRODUCT #%d : %s\n", p.getId(), p.getName());
        printDivider();
        System.out.printf("  Brand       : %s\n", p.getBrand());
        System.out.printf("  Category    : %s\n", p.getCategory());
        System.out.printf("  Rating      : ★ %.1f / 5.0\n", p.getRating());
        System.out.printf("  Price       : %s", formatCurrency(p.getPrice()));

        if (p.hasDiscount()) {
            System.out.printf("  (Original: %s | Save %d%%)\n",
                    formatCurrency(p.getOriginalPrice()), p.getDiscountPercentage());
        } else {
            System.out.println();
        }

        System.out.printf("  Stock Status: %s (%d units available)\n",
                p.getStock() > 0 ? "IN STOCK" : "OUT OF STOCK", p.getStock());
        if (p.isFeatured()) {
            System.out.println("  Highlight   : ★ Featured Luxury Pick");
        }
        System.out.println("  Description :");
        System.out.println("    " + wrapText(p.getDescription(), 72, "    "));
        printDoubleDivider();
    }

    /**
     * Prints shopping cart items table.
     */
    public static void printCartTable(List<CartItem> items, BigDecimal subtotal) {
        if (items == null || items.isEmpty()) {
            System.out.println("  (Your shopping cart is currently empty)");
            return;
        }

        printDoubleDivider();
        System.out.printf(" %-4s %-6s %-32s %-12s %-6s %-14s\n",
                "#", "PID", "Item Name", "Unit Price", "Qty", "Total");
        printDivider();

        int idx = 1;
        for (CartItem item : items) {
            String name = item.getProduct().getName();
            if (name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            System.out.printf(" %-4d %-6d %-32s %-12s %-6d %-14s\n",
                    idx++,
                    item.getProduct().getId(),
                    name,
                    formatCurrency(item.getProduct().getPrice()),
                    item.getQuantity(),
                    formatCurrency(item.getSubtotal()));
        }
        printDivider();
        System.out.printf(" %-62s : %14s\n", "CART SUBTOTAL", formatCurrency(subtotal));
        printDoubleDivider();
    }

    /**
     * Prints orders table.
     */
    public static void printOrdersTable(List<Order> orders, boolean showCustomerName) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("  (No orders recorded yet)");
            return;
        }

        printDoubleDivider();
        if (showCustomerName) {
            System.out.printf(" %-18s %-16s %-16s %-6s %-12s %-10s\n",
                    "Order ID", "Date", "Customer", "Items", "Amount", "Status");
        } else {
            System.out.printf(" %-18s %-18s %-6s %-14s %-12s\n",
                    "Order ID", "Date", "Items", "Total Amount", "Status");
        }
        printDivider();

        for (Order o : orders) {
            int itemCount = o.getItems().stream().mapToInt(CartItem::getQuantity).sum();
            if (showCustomerName) {
                String cname = o.getCustomerName();
                if (cname.length() > 14) cname = cname.substring(0, 12) + "..";
                System.out.printf(" %-18s %-16s %-16s %-6d %-12s %-10s\n",
                        o.getOrderId(), o.getFormattedDate(), cname, itemCount,
                        formatCurrency(o.getTotalAmount()), o.getStatus());
            } else {
                System.out.printf(" %-18s %-18s %-6d %-14s %-12s\n",
                        o.getOrderId(), o.getFormattedDate(), itemCount,
                        formatCurrency(o.getTotalAmount()), o.getStatus());
            }
        }
        printDoubleDivider();
    }

    private static String wrapText(String text, int maxWidth, String indent) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        String[] words = text.split(" ");
        int currentLen = 0;
        for (String word : words) {
            if (currentLen + word.length() + 1 > maxWidth) {
                sb.append("\n").append(indent);
                currentLen = 0;
            } else if (currentLen > 0) {
                sb.append(" ");
                currentLen++;
            }
            sb.append(word);
            currentLen += word.length();
        }
        return sb.toString();
    }
}
