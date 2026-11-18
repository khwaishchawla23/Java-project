package com.maisonelle.console;

import com.maisonelle.manager.OrderManager;
import com.maisonelle.manager.ProductManager;
import com.maisonelle.manager.UserManager;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Order;
import com.maisonelle.model.Product;
import com.maisonelle.util.InvoiceGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the complete administrator back-office console experience.
 * Strictly Java 21 Standard JDK Built-ins only.
 */
public class AdminMenu {

    private final InputHelper input;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final OrderManager orderManager;

    public AdminMenu(InputHelper input,
                     UserManager userManager,
                     ProductManager productManager,
                     OrderManager orderManager) {
        this.input = input;
        this.userManager = userManager;
        this.productManager = productManager;
        this.orderManager = orderManager;
    }

    public void display() {
        Customer admin = userManager.getCurrentUser();
        if (admin == null || !admin.isAdmin()) {
            ConsoleFormatter.printError("Access denied! This area is restricted to administrators.");
            return;
        }

        boolean active = true;
        while (active) {
            ConsoleFormatter.printSectionHeader("MAISONELLE ADMINISTRATOR PORTAL");
            System.out.println("  Logged in as: " + admin.getFullName() + " [ADMINISTRATOR]");
            displayKpiCards();
            ConsoleFormatter.printDivider();
            System.out.println("  1. View All Products Catalogue");
            System.out.println("  2. Add New Product");
            System.out.println("  3. Edit Product Details (Name, Brand, Category, Description)");
            System.out.println("  4. Update Product Price");
            System.out.println("  5. Update Product Stock / Inventory");
            System.out.println("  6. Delete Product from Catalogue");
            System.out.println("  7. View All Customer Orders");
            System.out.println("  8. Update Order Status (Processing / Shipped / Delivered / Cancelled)");
            System.out.println("  9. View Low Stock Alerts");
            System.out.println("  10. View Detailed Sales Summary");
            System.out.println("  0. Logout");
            ConsoleFormatter.printDivider();

            int choice = input.readInt("Select administrative action (0-10): ", 0, 10);
            switch (choice) {
                case 1 -> viewAllProducts();
                case 2 -> addNewProduct();
                case 3 -> editProduct();
                case 4 -> updatePrice();
                case 5 -> updateStock();
                case 6 -> deleteProduct();
                case 7 -> viewAllOrders();
                case 8 -> updateOrderStatus();
                case 9 -> viewLowStockAlerts();
                case 10 -> viewSalesSummary();
                case 0 -> {
                    userManager.logout();
                    ConsoleFormatter.printSuccess("Admin logged out successfully.");
                    active = false;
                }
            }
        }
    }

    private void displayKpiCards() {
        BigDecimal totalSales = orderManager.getTotalSales();
        int totalOrders = orderManager.getTotalOrdersCount();
        int totalCustomers = userManager.getTotalCustomerCount();
        long lowStockCount = productManager.getAllProducts().stream().filter(p -> p.getStock() < 5).count();

        System.out.println();
        System.out.println("  ┌──────────────────────┬──────────────────────┬──────────────────────┬──────────────────────┐");
        System.out.println("  │ GROSS REVENUE        │ TOTAL ORDERS         │ REGISTERED USERS     │ LOW STOCK ALERTS     │");
        System.out.printf("  │ %-20s │ %-20d │ %-20d │ %-20d │\n",
                ConsoleFormatter.formatCurrency(totalSales), totalOrders, totalCustomers, lowStockCount);
        System.out.println("  └──────────────────────┴──────────────────────┴──────────────────────┴──────────────────────┘");
    }

    private void viewAllProducts() {
        ConsoleFormatter.printSectionHeader("PRODUCT CATALOGUE (ADMIN VIEW)");
        List<Product> products = productManager.getAllProducts();
        ConsoleFormatter.printProductTable(products);
        input.pause();
    }

    private void addNewProduct() {
        ConsoleFormatter.printSectionHeader("ADD NEW PRODUCT");
        String name = input.readString("Enter product name: ", false);
        String category = input.readString("Enter category (e.g. Living Room, Bedroom, Dining, Lighting, Decor, Kitchen): ", false);
        String brand = input.readString("Enter brand name: ", false);
        String description = input.readString("Enter description: ", false);
        BigDecimal price = input.readBigDecimal("Enter selling price (₹): ", BigDecimal.ZERO);
        BigDecimal origPrice = input.readBigDecimalWithDefault("Enter original MRP / Price (₹)", price, BigDecimal.ZERO);
        int stock = input.readInt("Enter initial inventory stock: ", 0, 10000);
        boolean featured = input.readConfirmation("Mark as featured luxury pick?");

        Product p = new Product(0, name, category, description, price, origPrice, stock, "", 5.0, brand, featured);
        boolean added = productManager.addProduct(p);
        if (added) {
            ConsoleFormatter.printSuccess("Product \"" + name + "\" added successfully with ID #" + p.getId() + "!");
        } else {
            ConsoleFormatter.printError("Failed to add product.");
        }
        input.pause();
    }

    private void editProduct() {
        ConsoleFormatter.printSectionHeader("EDIT PRODUCT INFORMATION");
        int pid = input.readInt("Enter Product ID to edit: ", 1, 99999);
        Product p = productManager.getProductById(pid);
        if (p == null) {
            ConsoleFormatter.printError("Product ID #" + pid + " does not exist.");
            input.pause();
            return;
        }

        ConsoleFormatter.printProductCard(p);
        System.out.println("Leave inputs blank to keep current values.");

        String newName = input.readStringWithDefault("Product Name", p.getName());
        String newCategory = input.readStringWithDefault("Category", p.getCategory());
        String newBrand = input.readStringWithDefault("Brand", p.getBrand());
        String newDesc = input.readStringWithDefault("Description", p.getDescription());

        p.setName(newName);
        p.setCategory(newCategory);
        p.setBrand(newBrand);
        p.setDescription(newDesc);

        boolean updated = productManager.updateProduct(p);
        if (updated) {
            ConsoleFormatter.printSuccess("Product details updated successfully!");
        } else {
            ConsoleFormatter.printError("Failed to update product details.");
        }
        input.pause();
    }

    private void updatePrice() {
        ConsoleFormatter.printSectionHeader("UPDATE PRODUCT PRICE");
        int pid = input.readInt("Enter Product ID: ", 1, 99999);
        Product p = productManager.getProductById(pid);
        if (p == null) {
            ConsoleFormatter.printError("Product not found.");
            input.pause();
            return;
        }

        System.out.printf("Current Price: %s\n", ConsoleFormatter.formatCurrency(p.getPrice()));
        BigDecimal newPrice = input.readBigDecimal("Enter new price (₹): ", BigDecimal.ZERO);
        boolean updated = productManager.updatePrice(pid, newPrice);
        if (updated) {
            ConsoleFormatter.printSuccess("Price for \"" + p.getName() + "\" updated to " + ConsoleFormatter.formatCurrency(newPrice));
        } else {
            ConsoleFormatter.printError("Failed to update price.");
        }
        input.pause();
    }

    private void updateStock() {
        ConsoleFormatter.printSectionHeader("UPDATE INVENTORY STOCK");
        int pid = input.readInt("Enter Product ID: ", 1, 99999);
        Product p = productManager.getProductById(pid);
        if (p == null) {
            ConsoleFormatter.printError("Product not found.");
            input.pause();
            return;
        }

        System.out.printf("Current Stock for \"%s\": %d units\n", p.getName(), p.getStock());
        int newStock = input.readInt("Enter new total stock count: ", 0, 100000);
        boolean updated = productManager.updateStock(pid, newStock);
        if (updated) {
            ConsoleFormatter.printSuccess("Stock updated to " + newStock + " units.");
        } else {
            ConsoleFormatter.printError("Failed to update stock.");
        }
        input.pause();
    }

    private void deleteProduct() {
        ConsoleFormatter.printSectionHeader("DELETE PRODUCT");
        int pid = input.readInt("Enter Product ID to delete: ", 1, 99999);
        Product p = productManager.getProductById(pid);
        if (p == null) {
            ConsoleFormatter.printError("Product not found.");
            input.pause();
            return;
        }

        System.out.printf("Product to delete: #%d - %s (%s)\n", p.getId(), p.getName(), ConsoleFormatter.formatCurrency(p.getPrice()));
        if (input.readConfirmation("Are you sure you want to permanently remove this product?")) {
            boolean deleted = productManager.deleteProduct(pid);
            if (deleted) {
                ConsoleFormatter.printSuccess("Product successfully deleted from catalogue.");
            } else {
                ConsoleFormatter.printError("Could not delete product.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        input.pause();
    }

    private void viewAllOrders() {
        ConsoleFormatter.printSectionHeader("ALL CUSTOMER ORDERS");
        List<Order> orders = orderManager.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("  No customer orders recorded yet.");
            input.pause();
            return;
        }

        ConsoleFormatter.printOrdersTable(orders, true);

        System.out.println("\nOptions:");
        System.out.println("  1. Inspect Full Order Tax Invoice");
        System.out.println("  0. Back");
        int opt = input.readInt("Select option (0-1): ", 0, 1);
        if (opt == 1) {
            String oid = input.readString("Enter Order ID to inspect: ", false);
            Order o = orderManager.getOrderById(oid);
            if (o != null) {
                if (o.getInvoiceFilePath() != null) {
                    ConsoleFormatter.printDoubleDivider();
                    InvoiceGenerator.printInvoiceToTerminal(new File(o.getInvoiceFilePath()));
                    ConsoleFormatter.printDoubleDivider();
                } else {
                    ConsoleFormatter.printWarning("Invoice file path not recorded.");
                }
            } else {
                ConsoleFormatter.printError("Order ID not found.");
            }
            input.pause();
        }
    }

    private void updateOrderStatus() {
        ConsoleFormatter.printSectionHeader("UPDATE ORDER FULFILLMENT STATUS");
        String oid = input.readString("Enter Order ID: ", false);
        Order o = orderManager.getOrderById(oid);
        if (o == null) {
            ConsoleFormatter.printError("Order ID not found.");
            input.pause();
            return;
        }

        System.out.printf("Order #%s for %s | Current Status: %s\n",
                o.getOrderId(), o.getCustomerName(), o.getStatus());
        System.out.println("Select new status:");
        System.out.println("  1. CONFIRMED");
        System.out.println("  2. PROCESSING");
        System.out.println("  3. SHIPPED");
        System.out.println("  4. DELIVERED");
        System.out.println("  5. CANCELLED");

        int choice = input.readInt("Select status (1-5): ", 1, 5);
        Order.Status newStatus = switch (choice) {
            case 1 -> Order.Status.CONFIRMED;
            case 2 -> Order.Status.PROCESSING;
            case 3 -> Order.Status.SHIPPED;
            case 4 -> Order.Status.DELIVERED;
            default -> Order.Status.CANCELLED;
        };

        boolean updated = orderManager.updateOrderStatus(oid, newStatus);
        if (updated) {
            ConsoleFormatter.printSuccess("Order #" + oid + " status updated to " + newStatus);
        } else {
            ConsoleFormatter.printError("Failed to update status.");
        }
        input.pause();
    }

    private void viewLowStockAlerts() {
        ConsoleFormatter.printSectionHeader("LOW STOCK INVENTORY ALERTS (< 5 UNITS)");
        List<Product> lowStock = productManager.getAllProducts().stream()
                .filter(p -> p.getStock() < 5)
                .collect(Collectors.toList());

        if (lowStock.isEmpty()) {
            ConsoleFormatter.printSuccess("All products have healthy inventory levels (>= 5 units).");
        } else {
            ConsoleFormatter.printDoubleDivider();
            System.out.printf(" %-5s %-32s %-16s %-10s\n", "PID", "Product Name", "Category", "Stock Left");
            ConsoleFormatter.printDivider();
            for (Product p : lowStock) {
                System.out.printf(" %-5d %-32s %-16s %-10s\n",
                        p.getId(), p.getName(), p.getCategory(),
                        p.getStock() == 0 ? "OUT OF STOCK" : p.getStock() + " remaining");
            }
            ConsoleFormatter.printDoubleDivider();
        }
        input.pause();
    }

    private void viewSalesSummary() {
        ConsoleFormatter.printSectionHeader("EXECUTIVE SALES & REVENUE SUMMARY");
        BigDecimal totalSales = orderManager.getTotalSales();
        int totalOrders = orderManager.getTotalOrdersCount();
        int totalCustomers = userManager.getTotalCustomerCount();
        int totalProducts = productManager.getAllProducts().size();

        System.out.printf("  Total Gross Sales Revenue   : %s\n", ConsoleFormatter.formatCurrency(totalSales));
        System.out.printf("  Total Orders Placed         : %d\n", totalOrders);
        System.out.printf("  Total Registered Customers  : %d\n", totalCustomers);
        System.out.printf("  Total Products in Catalogue : %d\n", totalProducts);

        if (totalOrders > 0) {
            BigDecimal avgOrderVal = totalSales.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);
            System.out.printf("  Average Order Value (AOV)   : %s\n", ConsoleFormatter.formatCurrency(avgOrderVal));
        }

        ConsoleFormatter.printDoubleDivider();
        input.pause();
    }
}
