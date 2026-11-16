package com.maisonelle;

import com.maisonelle.manager.*;
import com.maisonelle.model.*;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.PasswordUtil;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

/**
 * Automated Test Runner for Maisonelle.
 * Validates models, business rules, cryptographic authentication,
 * billing calculations, order processing, and text invoice generation.
 */
public class MaisonelleTestRunner {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   MAISONELLE AUTOMATED VERIFICATION SUITE");
        System.out.println("   Pure Java 21 Standard JDK Only");
        System.out.println("=================================================");

        testModelAndDataInitialization();
        testProductCatalogueAndFiltering();
        testCartOperations();
        testWishlistOperations();
        testAuthenticationAndSecurity();
        testBillingAndDiscountService();
        testOrderPlacementAndStockDeduction();
        testAdminManagement();

        System.out.println("\n=================================================");
        System.out.printf("   TEST SUMMARY: %d Passed, %d Failed (Total: %d)\n",
                testsPassed, testsFailed, testsRun);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message + " - Condition was false");
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        testsRun++;
        if (expected == null && actual == null) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else if (expected != null && expected.equals(actual)) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message + " - Expected: " + expected + ", Got: " + actual);
        }
    }

    // 1. DATA INITIALIZATION
    private static void testModelAndDataInitialization() {
        System.out.println("\n--- Testing Module 1: Seed Data & Models ---");
        DataManager dm = new DataManager();
        ProductManager pm = new ProductManager(dm);

        assertTrue(pm.getTotalProductsCount() >= 45, "Catalogue contains at least 45 initial products (Actual: " + pm.getTotalProductsCount() + ")");
        Product p101 = pm.getProductById(101);
        assertTrue(p101 != null, "Found product 101: Sony Headphones");
        assertEquals("Sony", p101.getBrand(), "Product 101 brand is Sony");
        assertTrue(p101.hasDiscount(), "Product 101 has discount");
        assertTrue(p101.getDiscountPercentage() > 0, "Discount percentage calculated correctly");
    }

    // 2. PRODUCT CATALOGUE, SEARCH & FILTERS
    private static void testProductCatalogueAndFiltering() {
        System.out.println("\n--- Testing Module 2: Catalogue Search & Sorting ---");
        DataManager dm = new DataManager();
        ProductManager pm = new ProductManager(dm);

        List<Product> electronics = pm.filterAndSort("Electronics", "", "Featured & Curated");
        assertTrue(electronics.size() >= 10, "Electronics category filter returns at least 10 products");

        List<Product> searchSony = pm.filterAndSort("All", "Sony", "Featured & Curated");
        assertTrue(!searchSony.isEmpty(), "Search query 'Sony' returns matching products");
        assertTrue(searchSony.get(0).getName().contains("Sony"), "Search result contains Sony in title");

        List<Product> sortedAsc = pm.filterAndSort("All", "", "Price: Low to High");
        boolean ascending = true;
        for (int i = 0; i < sortedAsc.size() - 1; i++) {
            if (sortedAsc.get(i).getPrice().compareTo(sortedAsc.get(i + 1).getPrice()) > 0) {
                ascending = false;
                break;
            }
        }
        assertTrue(ascending, "Sort Price: Low to High accurately orders products");
    }

    // 3. CART OPERATIONS
    private static void testCartOperations() {
        System.out.println("\n--- Testing Module 3: Shopping Cart Logic ---");
        Cart cart = new Cart();
        Product p = new Product(999, "Silk Scarf", "Accessories", "Desc",
                new BigDecimal("1500.00"), 5, "placeholder.jpg", 4.8, "Maisonelle", true);

        assertTrue(cart.isEmpty(), "Cart is initially empty");
        boolean added = cart.addItem(p, 2);
        assertTrue(added, "Added 2 units to cart");
        assertEquals(2, cart.getTotalItemCount(), "Total item count is 2");
        assertEquals(new BigDecimal("3000.00"), cart.getSubtotal(), "Subtotal is ₹3,000.00");

        boolean exceed = cart.addItem(p, 10);
        assertTrue(!exceed, "Cannot add more items than available in stock");

        cart.updateQuantity(999, 4);
        assertEquals(4, cart.getTotalItemCount(), "Updated quantity to 4 successfully");

        cart.removeItem(999);
        assertTrue(cart.isEmpty(), "Removed item; cart is empty");
    }

    // 4. WISHLIST OPERATIONS
    private static void testWishlistOperations() {
        System.out.println("\n--- Testing Module 4: Wishlist Management ---");
        DataManager dm = new DataManager();
        ProductManager pm = new ProductManager(dm);
        WishlistManager wm = new WishlistManager(dm, pm);
        CartManager cm = new CartManager();

        wm.removeFromWishlist(101); // Clean slate
        boolean added = wm.addToWishlist(101);
        assertTrue(added, "Added product 101 to wishlist");
        assertTrue(wm.isInWishlist(101), "Product 101 is in wishlist");

        boolean duplicate = wm.addToWishlist(101);
        assertTrue(!duplicate, "Duplicate wishlist entry prevented");

        boolean moved = wm.moveToCart(101, cm);
        assertTrue(moved, "Item moved from wishlist to cart");
        assertTrue(!wm.isInWishlist(101), "Item removed from wishlist after move");
        assertEquals(1, cm.getTotalItemCount(), "Item present in cart");
    }

    // 5. AUTHENTICATION & SECURITY
    private static void testAuthenticationAndSecurity() {
        System.out.println("\n--- Testing Module 5: Authentication & PBKDF2 Cryptography ---");
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("secret123", salt);

        assertTrue(PasswordUtil.verifyPassword("secret123", hash, salt), "Valid password successfully verified");
        assertTrue(!PasswordUtil.verifyPassword("wrongpass", hash, salt), "Invalid password correctly rejected");

        DataManager dm = new DataManager();
        UserManager um = new UserManager(dm);

        try {
            Customer admin = um.authenticate("admin@maisonelle.com", "admin123");
            assertTrue(admin != null && admin.isAdmin(), "Demo administrator successfully authenticated");
        } catch (Exception e) {
            assertTrue(false, "Admin auth failed: " + e.getMessage());
        }

        try {
            Customer customer = um.authenticate("clara@maisonelle.com", "customer123");
            assertTrue(customer != null && !customer.isAdmin(), "Demo customer Clara successfully authenticated");
        } catch (Exception e) {
            assertTrue(false, "Customer auth failed: " + e.getMessage());
        }
    }

    // 6. BILLING & DISCOUNTS
    private static void testBillingAndDiscountService() {
        System.out.println("\n--- Testing Module 6: Billing & Statutory GST ---");
        DataManager dm = new DataManager();
        DiscountService ds = new DiscountService(dm);
        BillingService bs = new BillingService(ds);

        Cart cart = new Cart();
        Product item = new Product(888, "Cashmere Cardigan", "Fashion", "Desc",
                new BigDecimal("2000.00"), 10, "placeholder.jpg", 4.9, "Maisonelle", true);
        cart.addItem(item, 1);

        // WELCOME10 coupon (10% off on >= 1000)
        DiscountService.CouponResult cr1 = ds.applyCoupon("WELCOME10", cart.getSubtotal());
        assertTrue(cr1.isSuccess(), "WELCOME10 successfully applied");
        assertEquals(new BigDecimal("200.00"), cr1.getDiscountAmount(), "10% of 2000.00 is 200.00");

        // Billing summary calculation
        BillingService.BillingSummary summary = bs.calculateBilling(cart, "WELCOME10");
        assertEquals(new BigDecimal("2000.00"), summary.getSubtotal(), "Subtotal is 2000.00");
        assertEquals(new BigDecimal("200.00"), summary.getDiscountAmount(), "Discount is 200.00");
        assertEquals(new BigDecimal("1800.00"), summary.getTaxableAmount(), "Taxable amount is 1800.00");

        // 18% GST on 1800.00 is 324.00
        assertEquals(new BigDecimal("324.00"), summary.getGstAmount(), "18% GST on 1800.00 is 324.00");

        // Taxable >= 1000 so delivery is free
        assertTrue(summary.isFreeDeliveryApplied(), "Free delivery applied for order >= 1000");
        assertEquals(new BigDecimal("0.00"), summary.getDeliveryFee(), "Delivery fee is 0.00");

        // Total = 1800 + 324 + 0 = 2124.00
        assertEquals(new BigDecimal("2124.00"), summary.getFinalPayableAmount(), "Final payable amount is 2124.00");
    }

    // 7. ORDER PLACEMENT & INVENTORY DEDUCTION
    private static void testOrderPlacementAndStockDeduction() {
        System.out.println("\n--- Testing Module 7: Order Fulfillment & Invoices ---");
        DataManager dm = new DataManager();
        ProductManager pm = new ProductManager(dm);
        InvoiceGenerator ig = new InvoiceGenerator(new File("invoices"));
        OrderManager om = new OrderManager(dm, pm, ig);
        DiscountService ds = new DiscountService(dm);
        BillingService bs = new BillingService(ds);

        Product p = pm.getProductById(101);
        int initialStock = p.getStock();

        Cart cart = new Cart();
        cart.addItem(p, 1);
        BillingService.BillingSummary summary = bs.calculateBilling(cart, null);

        Customer cust = new Customer(99, "Test Buyer", "buyer@test.com", "9999999999", "12 Park St", "Delhi", "110001", "hash", "salt", Customer.Role.CUSTOMER);

        try {
            Order order = om.placeOrder(cust, cart, summary, "12 Park St, Delhi - 110001", "UPI");
            assertTrue(order != null, "Order successfully placed");
            assertTrue(order.getOrderId().startsWith("ORD-"), "Order ID has proper prefix");

            // Verify stock deduction
            Product pAfter = pm.getProductById(101);
            assertEquals(initialStock - 1, pAfter.getStock(), "Inventory reduced by 1 after order");

            // Verify invoice file generated
            File invoiceFile = new File(order.getInvoiceFilePath());
            assertTrue(invoiceFile.exists(), "Text invoice file generated on disk");

            String content = Files.readString(invoiceFile.toPath());
            assertTrue(content.contains("MAISONELLE"), "Invoice contains Maisonelle branding");
            assertTrue(content.contains(order.getOrderId()), "Invoice contains Order ID");
            assertTrue(content.contains("TAX INVOICE"), "Invoice contains Tax Invoice header");

        } catch (Exception e) {
            assertTrue(false, "Order placement exception: " + e.getMessage());
        }
    }

    // 8. ADMIN MANAGEMENT
    private static void testAdminManagement() {
        System.out.println("\n--- Testing Module 8: Administrator Capabilities ---");
        DataManager dm = new DataManager();
        ProductManager pm = new ProductManager(dm);

        Product newP = new Product(0, "Aura Linen Throw", "Home & Living", "Warm textured throw",
                new BigDecimal("2499.00"), 15, "bedsheet.jpg", 4.7, "Maisonelle", true);

        boolean added = pm.addProduct(newP);
        assertTrue(added, "Admin added new product");
        int newId = newP.getId();
        assertTrue(newId > 0, "Product ID auto-assigned");

        pm.updatePrice(newId, new BigDecimal("2299.00"));
        assertEquals(new BigDecimal("2299.00"), pm.getProductById(newId).getPrice(), "Admin updated price");

        pm.updateStock(newId, 25);
        assertEquals(25, pm.getProductById(newId).getStock(), "Admin updated stock");

        boolean deleted = pm.deleteProduct(newId);
        assertTrue(deleted, "Admin deleted product");
        assertTrue(pm.getProductById(newId) == null, "Product confirmed deleted from catalogue");
    }
}
