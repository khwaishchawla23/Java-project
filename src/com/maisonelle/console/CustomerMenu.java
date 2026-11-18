package com.maisonelle.console;

import com.maisonelle.manager.*;
import com.maisonelle.model.*;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.InvoiceGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Handles the complete customer terminal shopping experience.
 * Strictly Java 21 Standard JDK Built-ins only.
 */
public class CustomerMenu {

    private final InputHelper input;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final OrderManager orderManager;
    private final BillingService billingService;
    private final DiscountService discountService;
    private final InvoiceGenerator invoiceGenerator;

    private String activeCouponCode = null;

    public CustomerMenu(InputHelper input,
                        UserManager userManager,
                        ProductManager productManager,
                        CartManager cartManager,
                        WishlistManager wishlistManager,
                        OrderManager orderManager,
                        BillingService billingService,
                        DiscountService discountService,
                        InvoiceGenerator invoiceGenerator) {
        this.input = input;
        this.userManager = userManager;
        this.productManager = productManager;
        this.cartManager = cartManager;
        this.wishlistManager = wishlistManager;
        this.orderManager = orderManager;
        this.billingService = billingService;
        this.discountService = discountService;
        this.invoiceGenerator = invoiceGenerator;
    }

    public void display() {
        Customer customer = userManager.getCurrentUser();
        if (customer == null) {
            ConsoleFormatter.printError("You must be logged in to access the boutique.");
            return;
        }

        boolean active = true;
        while (active) {
            ConsoleFormatter.printSectionHeader("CUSTOMER BOUTIQUE DASHBOARD");
            System.out.println("  Welcome, " + customer.getFullName() + " (" + customer.getEmail() + ")");
            System.out.printf("  Cart: %d item(s) (%s) | Wishlist: %d item(s)\n",
                    cartManager.getTotalItemCount(),
                    ConsoleFormatter.formatCurrency(cartManager.getSubtotal()),
                    wishlistManager.getWishlistCount());
            ConsoleFormatter.printDivider();
            System.out.println("  1. Browse All Products");
            System.out.println("  2. Search Products by Keyword");
            System.out.println("  3. Filter Products by Category");
            System.out.println("  4. Sort Products (Price, Rating, Name)");
            System.out.println("  5. View Product Details & Add to Cart / Wishlist");
            System.out.println("  6. View Shopping Cart & Manage Items");
            System.out.println("  7. View Saved Wishlist");
            System.out.println("  8. Proceed to Checkout & Place Order");
            System.out.println("  9. View Order History & Invoices");
            System.out.println("  10. View My Profile");
            System.out.println("  0. Logout");
            ConsoleFormatter.printDivider();

            int choice = input.readInt("Select option (0-10): ", 0, 10);
            switch (choice) {
                case 1 -> browseProducts();
                case 2 -> searchProducts();
                case 3 -> filterByCategory();
                case 4 -> sortProducts();
                case 5 -> viewProductDetailsFlow();
                case 6 -> manageCart();
                case 7 -> manageWishlist();
                case 8 -> checkoutFlow();
                case 9 -> viewOrderHistory();
                case 10 -> viewProfile();
                case 0 -> {
                    userManager.logout();
                    ConsoleFormatter.printSuccess("Logged out successfully. Have a wonderful day!");
                    active = false;
                }
            }
        }
    }

    private void browseProducts() {
        ConsoleFormatter.printSectionHeader("ALL PRODUCTS CATALOGUE");
        List<Product> list = productManager.getAllProducts();
        paginateProducts(list, "Catalogue");
    }

    private void searchProducts() {
        ConsoleFormatter.printSectionHeader("SEARCH PRODUCTS");
        String query = input.readString("Enter search keyword (e.g. Sofa, Velvet, Wood, Lamp): ", false);
        List<Product> matches = productManager.search(query);
        paginateProducts(matches, "Search Results for '" + query + "'");
    }

    private void filterByCategory() {
        ConsoleFormatter.printSectionHeader("FILTER BY CATEGORY");
        List<String> categories = productManager.getCategories();
        System.out.println("  Available Categories:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + categories.get(i));
        }
        int choice = input.readInt("Select category (1-" + categories.size() + "): ", 1, categories.size());
        String selectedCategory = categories.get(choice - 1);
        List<Product> filtered = productManager.filterByCategory(selectedCategory);
        paginateProducts(filtered, "Category: " + selectedCategory);
    }

    private void sortProducts() {
        ConsoleFormatter.printSectionHeader("SORT PRODUCTS");
        System.out.println("  1. Price: Low to High");
        System.out.println("  2. Price: High to Low");
        System.out.println("  3. Customer Rating (Highest First)");
        System.out.println("  4. Product Name (A to Z)");
        int choice = input.readInt("Select sorting criteria (1-4): ", 1, 4);

        String sortKey = switch (choice) {
            case 1 -> "Price: Low to High";
            case 2 -> "Price: High to Low";
            case 3 -> "Rating";
            default -> "Name";
        };

        List<Product> sorted = productManager.filterAndSort("All", "", sortKey);
        paginateProducts(sorted, "Sorted by " + sortKey);
    }

    private void paginateProducts(List<Product> products, String title) {
        if (products == null || products.isEmpty()) {
            ConsoleFormatter.printWarning("No products found.");
            input.pause();
            return;
        }

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) products.size() / pageSize);
        int currentPage = 1;

        while (true) {
            int fromIndex = (currentPage - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, products.size());
            List<Product> pageItems = products.subList(fromIndex, toIndex);

            ConsoleFormatter.printSectionHeader(title + " (Page " + currentPage + " of " + totalPages + ")");
            ConsoleFormatter.printProductTable(pageItems);

            System.out.println("\nOptions:");
            System.out.println("  [N] Next Page | [P] Previous Page | [V] View Product Details | [B] Back to Menu");
            String action = input.readString("Action: ", false).trim().toUpperCase();

            if (action.equalsIgnoreCase("N")) {
                if (currentPage < totalPages) currentPage++;
                else ConsoleFormatter.printWarning("You are already on the last page.");
            } else if (action.equalsIgnoreCase("P")) {
                if (currentPage > 1) currentPage--;
                else ConsoleFormatter.printWarning("You are already on the first page.");
            } else if (action.equalsIgnoreCase("V")) {
                int pid = input.readInt("Enter Product ID to view: ", 1, 99999);
                showProductDetailsAndActions(pid);
            } else if (action.equalsIgnoreCase("B")) {
                break;
            } else {
                ConsoleFormatter.printWarning("Invalid choice.");
            }
        }
    }

    private void viewProductDetailsFlow() {
        ConsoleFormatter.printSectionHeader("VIEW PRODUCT DETAILS");
        int pid = input.readInt("Enter Product ID: ", 1, 99999);
        showProductDetailsAndActions(pid);
    }

    private void showProductDetailsAndActions(int productId) {
        Product p = productManager.getProductById(productId);
        if (p == null) {
            ConsoleFormatter.printError("Product with ID #" + productId + " was not found.");
            input.pause();
            return;
        }

        ConsoleFormatter.printProductCard(p);

        boolean inWishlist = wishlistManager.isInWishlist(p.getId());
        System.out.println("Actions:");
        System.out.println("  1. Add to Shopping Cart");
        System.out.println("  2. " + (inWishlist ? "Remove from Saved Wishlist" : "Save to Wishlist"));
        System.out.println("  0. Back");

        int act = input.readInt("Select action (0-2): ", 0, 2);
        if (act == 1) {
            if (p.getStock() <= 0) {
                ConsoleFormatter.printError("This product is currently out of stock.");
            } else {
                int qty = input.readInt("Enter quantity (1-" + p.getStock() + "): ", 1, p.getStock());
                boolean added = cartManager.addToCart(p, qty);
                if (added) {
                    ConsoleFormatter.printSuccess("Added " + qty + " unit(s) of \"" + p.getName() + "\" to cart!");
                } else {
                    ConsoleFormatter.printError("Failed to add to cart (exceeds inventory limits).");
                }
            }
            input.pause();
        } else if (act == 2) {
            boolean added = wishlistManager.toggleWishlist(p);
            if (added) {
                ConsoleFormatter.printSuccess("Saved \"" + p.getName() + "\" to your wishlist!");
            } else {
                ConsoleFormatter.printSuccess("Removed \"" + p.getName() + "\" from your wishlist.");
            }
            input.pause();
        }
    }

    private void manageCart() {
        boolean inCart = true;
        while (inCart) {
            ConsoleFormatter.printSectionHeader("YOUR SHOPPING CART");
            List<CartItem> items = cartManager.getItems();
            ConsoleFormatter.printCartTable(items, cartManager.getSubtotal());

            if (items.isEmpty()) {
                input.pause();
                return;
            }

            System.out.println("\nCart Options:");
            System.out.println("  1. Update Quantity of an Item");
            System.out.println("  2. Remove Item from Cart");
            System.out.println("  3. Clear Entire Cart");
            System.out.println("  4. Proceed to Checkout");
            System.out.println("  0. Return to Main Menu");

            int opt = input.readInt("Select cart option (0-4): ", 0, 4);
            switch (opt) {
                case 1 -> {
                    int pid = input.readInt("Enter Product ID to update: ", 1, 99999);
                    Product prod = productManager.getProductById(pid);
                    if (prod == null) {
                        ConsoleFormatter.printError("Product ID not found.");
                        break;
                    }
                    int maxStock = prod.getStock();
                    int newQty = input.readInt("Enter new quantity (0 to remove, max " + maxStock + "): ", 0, maxStock);
                    if (newQty == 0) {
                        cartManager.removeFromCart(pid);
                        ConsoleFormatter.printSuccess("Item removed from cart.");
                    } else {
                        boolean updated = cartManager.updateQuantity(pid, newQty);
                        if (updated) {
                            ConsoleFormatter.printSuccess("Cart quantity updated.");
                        } else {
                            ConsoleFormatter.printError("Could not update quantity.");
                        }
                    }
                }
                case 2 -> {
                    int pid = input.readInt("Enter Product ID to remove: ", 1, 99999);
                    boolean removed = cartManager.removeFromCart(pid);
                    if (removed) {
                        ConsoleFormatter.printSuccess("Item removed from cart.");
                    } else {
                        ConsoleFormatter.printError("Item not found in cart.");
                    }
                }
                case 3 -> {
                    if (input.readConfirmation("Are you sure you want to empty your entire cart?")) {
                        cartManager.clearCart();
                        ConsoleFormatter.printSuccess("Shopping cart cleared.");
                        inCart = false;
                    }
                }
                case 4 -> {
                    checkoutFlow();
                    inCart = false;
                }
                case 0 -> inCart = false;
            }
        }
    }

    private void manageWishlist() {
        boolean inWish = true;
        while (inWish) {
            ConsoleFormatter.printSectionHeader("SAVED WISHLIST");
            List<Product> wishlist = wishlistManager.getWishlistProducts();
            if (wishlist.isEmpty()) {
                System.out.println("  Your wishlist is empty. Discover luxury items in our catalogue!");
                input.pause();
                return;
            }

            ConsoleFormatter.printDoubleDivider();
            System.out.printf(" %-5s %-32s %-16s %-14s %-8s\n",
                    "PID", "Product Name", "Category", "Price", "Stock");
            ConsoleFormatter.printDivider();
            for (Product p : wishlist) {
                String name = p.getName();
                if (name.length() > 30) name = name.substring(0, 27) + "...";
                System.out.printf(" %-5d %-32s %-16s %-14s %-8s\n",
                        p.getId(), name, p.getCategory(),
                        ConsoleFormatter.formatCurrency(p.getPrice()),
                        p.getStock() > 0 ? p.getStock() + " left" : "OUT");
            }
            ConsoleFormatter.printDoubleDivider();

            System.out.println("\nWishlist Options:");
            System.out.println("  1. Move Item to Shopping Cart");
            System.out.println("  2. Remove Item from Wishlist");
            System.out.println("  0. Back to Main Menu");

            int opt = input.readInt("Select option (0-2): ", 0, 2);
            switch (opt) {
                case 1 -> {
                    int pid = input.readInt("Enter Product ID to move to cart: ", 1, 99999);
                    boolean moved = wishlistManager.moveToCart(pid, cartManager);
                    if (moved) {
                        ConsoleFormatter.printSuccess("Product moved from Wishlist to Shopping Cart!");
                    } else {
                        ConsoleFormatter.printError("Could not move product (either invalid ID or item out of stock).");
                    }
                }
                case 2 -> {
                    int pid = input.readInt("Enter Product ID to remove: ", 1, 99999);
                    boolean removed = wishlistManager.removeFromWishlist(pid);
                    if (removed) {
                        ConsoleFormatter.printSuccess("Product removed from wishlist.");
                    } else {
                        ConsoleFormatter.printError("Product was not in wishlist.");
                    }
                }
                case 0 -> inWish = false;
            }
        }
    }

    private void checkoutFlow() {
        if (cartManager.isEmpty()) {
            ConsoleFormatter.printError("Your shopping cart is empty! Add products before checking out.");
            input.pause();
            return;
        }

        Customer customer = userManager.getCurrentUser();
        ConsoleFormatter.printSectionHeader("BOUTIQUE CHECKOUT & BILLING");

        // 1. Coupon Section
        System.out.println("  Active Promotions Available:");
        for (Coupon c : discountService.getAllCoupons()) {
            if (c.isActive()) {
                System.out.printf("  • %-10s : %s (Min purchase: %s)\n",
                        c.getCode(), c.getDescription(), ConsoleFormatter.formatCurrency(c.getMinPurchase()));
            }
        }
        ConsoleFormatter.printDivider();

        if (input.readConfirmation("Would you like to apply or change a coupon code?")) {
            String code = input.readString("Enter coupon code (or press Enter to skip): ", true);
            if (!code.isEmpty()) {
                DiscountService.CouponResult res = discountService.applyCoupon(code, cartManager.getSubtotal());
                if (res.isSuccess()) {
                    activeCouponCode = res.getCode();
                    ConsoleFormatter.printSuccess(res.getMessage());
                } else {
                    ConsoleFormatter.printError(res.getMessage());
                }
            } else {
                activeCouponCode = null;
                System.out.println("  Coupon cleared.");
            }
        }

        // 2. Calculate Final Bill
        BillingService.BillingSummary summary = billingService.calculateBilling(cartManager.getCart(), activeCouponCode);

        ConsoleFormatter.printSectionHeader("BILLING SUMMARY");
        System.out.printf("  %-45s : %15s\n", "Cart Subtotal", ConsoleFormatter.formatCurrency(summary.getSubtotal()));
        if (summary.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("  %-45s : %15s\n",
                    "Promotional Discount (" + summary.getCouponCode() + ")",
                    "- " + ConsoleFormatter.formatCurrency(summary.getDiscountAmount()));
        }
        System.out.printf("  %-45s : %15s\n", "Taxable Amount", ConsoleFormatter.formatCurrency(summary.getTaxableAmount()));
        System.out.printf("  %-45s : %15s\n", "Statutory GST (18%)", ConsoleFormatter.formatCurrency(summary.getGstAmount()));
        System.out.printf("  %-45s : %15s\n", "Delivery Fee",
                summary.isFreeDeliveryApplied() ? "FREE (Orders >= ₹1,000)" : ConsoleFormatter.formatCurrency(summary.getDeliveryFee()));
        ConsoleFormatter.printDoubleDivider();
        System.out.printf("  %-45s : %15s\n", "FINAL PAYABLE AMOUNT", ConsoleFormatter.formatCurrency(summary.getFinalPayableAmount()));
        ConsoleFormatter.printDoubleDivider();

        // 3. Shipping Address
        ConsoleFormatter.printSubHeader("Shipping Address");
        String defaultAddr = customer.getAddress() + ", " + customer.getCity() + " - " + customer.getPincode();
        System.out.println("  Saved Address: " + defaultAddr);
        String shippingAddress = defaultAddr;
        if (input.readConfirmation("Ship to a different address?")) {
            shippingAddress = input.readString("Enter full delivery address: ", false);
        }

        // 4. Payment Method
        ConsoleFormatter.printSubHeader("Payment Method");
        System.out.println("  1. Instant UPI (GPay / PhonePe / Paytm)");
        System.out.println("  2. Credit / Debit Card (Visa / Mastercard / RuPay)");
        System.out.println("  3. Net Banking (All Major Indian Banks)");
        System.out.println("  4. Cash on Delivery (COD)");
        int payChoice = input.readInt("Select payment method (1-4): ", 1, 4);
        String paymentMethod = switch (payChoice) {
            case 1 -> "UPI";
            case 2 -> "Card";
            case 3 -> "Net Banking";
            default -> "Cash on Delivery";
        };

        // 5. Final Confirmation
        ConsoleFormatter.printDivider();
        System.out.printf("  Final Total: %s via %s\n",
                ConsoleFormatter.formatCurrency(summary.getFinalPayableAmount()), paymentMethod);
        boolean confirmed = input.readConfirmation("Confirm and place your luxury order now?");
        if (!confirmed) {
            ConsoleFormatter.printWarning("Checkout cancelled. Your cart items are preserved.");
            input.pause();
            return;
        }

        // 6. Execute Order Placement
        try {
            Order order = orderManager.placeOrder(customer, cartManager.getCart(), summary, shippingAddress, paymentMethod);
            cartManager.clearCart();
            activeCouponCode = null;

            ConsoleFormatter.printSectionHeader("ORDER PLACED SUCCESSFULLY!");
            System.out.println("  Congratulations, " + customer.getFullName() + "!");
            System.out.println("  Order ID         : " + order.getOrderId());
            System.out.println("  Order Date       : " + order.getFormattedDate());
            System.out.println("  Payment Method   : " + order.getPaymentMethod());
            System.out.println("  Invoice Generated: " + order.getInvoiceFilePath());
            ConsoleFormatter.printDivider();

            if (input.readConfirmation("Would you like to preview the tax invoice receipt right now in the terminal?")) {
                ConsoleFormatter.printDoubleDivider();
                InvoiceGenerator.printInvoiceToTerminal(new File(order.getInvoiceFilePath()));
                ConsoleFormatter.printDoubleDivider();
            }

        } catch (Exception e) {
            ConsoleFormatter.printError("Failed to complete order: " + e.getMessage());
        }

        input.pause();
    }

    private void viewOrderHistory() {
        Customer customer = userManager.getCurrentUser();
        ConsoleFormatter.printSectionHeader("YOUR ORDER HISTORY");
        List<Order> orders = orderManager.getOrdersForCustomer(customer.getId());

        if (orders.isEmpty()) {
            System.out.println("  You haven't placed any orders yet.");
            input.pause();
            return;
        }

        ConsoleFormatter.printOrdersTable(orders, false);

        System.out.println("\nOptions:");
        System.out.println("  1. View Full Order Invoice Details");
        System.out.println("  0. Back to Main Menu");
        int opt = input.readInt("Select option (0-1): ", 0, 1);
        if (opt == 1) {
            String oid = input.readString("Enter Order ID (e.g. ORD-...): ", false);
            Order o = orderManager.getOrderById(oid);
            if (o != null && o.getCustomerId() == customer.getId()) {
                if (o.getInvoiceFilePath() != null) {
                    ConsoleFormatter.printDoubleDivider();
                    InvoiceGenerator.printInvoiceToTerminal(new File(o.getInvoiceFilePath()));
                    ConsoleFormatter.printDoubleDivider();
                } else {
                    ConsoleFormatter.printWarning("Invoice file path not recorded for this order.");
                }
            } else {
                ConsoleFormatter.printError("Order not found or does not belong to your account.");
            }
            input.pause();
        }
    }

    private void viewProfile() {
        Customer c = userManager.getCurrentUser();
        ConsoleFormatter.printSectionHeader("CUSTOMER PROFILE");
        System.out.println("  Customer ID : #" + c.getId());
        System.out.println("  Full Name   : " + c.getFullName());
        System.out.println("  Email       : " + c.getEmail());
        System.out.println("  Phone       : " + c.getPhone());
        System.out.println("  Address     : " + c.getAddress());
        System.out.println("  City        : " + c.getCity());
        System.out.println("  Pincode     : " + c.getPincode());
        System.out.println("  Role        : " + c.getRole());
        ConsoleFormatter.printDivider();
        input.pause();
    }
}
