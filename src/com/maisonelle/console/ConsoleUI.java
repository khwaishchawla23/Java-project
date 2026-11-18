package com.maisonelle.console;

import com.maisonelle.manager.*;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Product;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;

import java.io.File;
import java.util.List;

/**
 * Main Console User Interface & Navigation Controller.
 * Strictly Java 21 Standard JDK Built-ins only.
 */
public class ConsoleUI {

    private final InputHelper input;
    private final DataManager dataManager;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final OrderManager orderManager;
    private final DiscountService discountService;
    private final BillingService billingService;
    private final InvoiceGenerator invoiceGenerator;

    private final CustomerMenu customerMenu;
    private final AdminMenu adminMenu;

    public ConsoleUI() {
        this.input = new InputHelper();
        this.dataManager = new DataManager();

        File invoiceDir = dataManager.getInvoiceDir();
        this.invoiceGenerator = new InvoiceGenerator(invoiceDir);

        this.productManager = new ProductManager(dataManager);
        this.userManager = new UserManager(dataManager);
        this.cartManager = new CartManager();
        this.wishlistManager = new WishlistManager(dataManager, productManager);
        this.orderManager = new OrderManager(dataManager, productManager, invoiceGenerator);
        this.discountService = new DiscountService(dataManager);
        this.billingService = new BillingService(discountService);

        this.customerMenu = new CustomerMenu(
                input, userManager, productManager, cartManager, wishlistManager,
                orderManager, billingService, discountService, invoiceGenerator
        );

        this.adminMenu = new AdminMenu(
                input, userManager, productManager, orderManager
        );
    }

    public void start() {
        boolean running = true;
        while (running) {
            ConsoleFormatter.printAppBanner();
            System.out.println("  1. Customer Login");
            System.out.println("  2. Administrator Login");
            System.out.println("  3. Register New Account");
            System.out.println("  4. Quick Demo Login (Preset Credentials)");
            System.out.println("  5. Browse Boutique Catalogue (Guest Mode)");
            System.out.println("  0. Exit");
            ConsoleFormatter.printDivider();

            int choice = input.readInt("Select option (0-5): ", 0, 5);
            switch (choice) {
                case 1 -> loginFlow(false);
                case 2 -> loginFlow(true);
                case 3 -> registerFlow();
                case 4 -> demoLoginFlow();
                case 5 -> guestBrowseFlow();
                case 0 -> {
                    System.out.println("\nThank you for visiting Maisonelle. Goodbye!");
                    running = false;
                }
            }
        }
    }

    private void loginFlow(boolean requireAdmin) {
        ConsoleFormatter.printSectionHeader(requireAdmin ? "ADMINISTRATOR LOGIN" : "CUSTOMER LOGIN");
        String email = input.readString("Email address: ", false);
        String password = input.readPassword("Password: ");

        try {
            Customer user = userManager.authenticate(email, password);
            if (requireAdmin && !user.isAdmin()) {
                ConsoleFormatter.printError("Access denied! This account does not possess administrator privileges.");
                userManager.logout();
                input.pause();
                return;
            }

            ConsoleFormatter.printSuccess("Welcome back, " + user.getFullName() + "!");
            if (user.isAdmin()) {
                adminMenu.display();
            } else {
                customerMenu.display();
            }

        } catch (Exception e) {
            ConsoleFormatter.printError(e.getMessage());
            input.pause();
        }
    }

    private void registerFlow() {
        ConsoleFormatter.printSectionHeader("REGISTER NEW CUSTOMER ACCOUNT");
        String name = input.readString("Full Name: ", false);
        String email = input.readString("Email address: ", false);
        String phone = input.readString("Phone number: ", false);
        String address = input.readString("Street address: ", false);
        String city = input.readString("City: ", false);
        String pincode = input.readString("Postal Pincode: ", false);
        String password = input.readPassword("Create Password (min 6 characters): ");

        try {
            Customer created = userManager.register(name, email, phone, address, city, pincode, password);
            userManager.setCurrentUser(created);
            ConsoleFormatter.printSuccess("Account created successfully! Welcome to Maisonelle, " + created.getFullName() + ".");
            input.pause();
            customerMenu.display();
        } catch (Exception e) {
            ConsoleFormatter.printError(e.getMessage());
            input.pause();
        }
    }

    private void demoLoginFlow() {
        ConsoleFormatter.printSectionHeader("QUICK DEMO LOGIN");
        System.out.println("  1. Customer Demo (Clara Bennett - clara@maisonelle.com / customer123)");
        System.out.println("  2. Administrator Demo (Maisonelle Admin - admin@maisonelle.com / admin123)");
        System.out.println("  0. Back");
        int choice = input.readInt("Select demo persona (0-2): ", 0, 2);

        try {
            if (choice == 1) {
                Customer user = userManager.authenticate("clara@maisonelle.com", "customer123");
                ConsoleFormatter.printSuccess("Authenticated as Customer: " + user.getFullName());
                customerMenu.display();
            } else if (choice == 2) {
                Customer user = userManager.authenticate("admin@maisonelle.com", "admin123");
                ConsoleFormatter.printSuccess("Authenticated as Administrator: " + user.getFullName());
                adminMenu.display();
            }
        } catch (Exception e) {
            ConsoleFormatter.printError("Demo authentication failed: " + e.getMessage());
            input.pause();
        }
    }

    private void guestBrowseFlow() {
        ConsoleFormatter.printSectionHeader("GUEST CATALOGUE BROWSER");
        List<Product> products = productManager.getAllProducts();
        ConsoleFormatter.printProductTable(products);
        System.out.println("\nLog in or register to add items to your shopping cart or place orders.");
        input.pause();
    }
}
