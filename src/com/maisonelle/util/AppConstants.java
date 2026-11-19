package com.maisonelle.util;

import java.math.BigDecimal;

/**
 * Global application constants for the Maisonelle Store Management System.
 * Demonstrates:
 * - Utility class design (private constructor to prevent instantiation).
 * - public static final constants for application-wide immutable settings.
 * - BigDecimal constants to preserve financial arithmetic accuracy.
 */
public final class AppConstants {

    // Private constructor prevents instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a static utility class and cannot be instantiated.");
    }

    // Application Branding & Metadata
    public static final String APP_NAME = "Maisonelle";
    public static final String APP_TAGLINE = "A little luxury, thoughtfully curated.";
    public static final String APP_VERSION = "2.1.0-STABLE";

    // Financial & Taxation Constants
    public static final String CURRENCY_SYMBOL = "₹";
    public static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% Statutory GST
    public static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("1000.00");
    public static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("99.00");

    // Inventory & Operational Thresholds
    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int DEFAULT_CATEGORY_COUNT = 5;
    public static final int MONTHS_IN_YEAR = 12;

    // File Persistence Constants
    public static final String DATA_DIRECTORY = "data";
    public static final String INVOICES_DIRECTORY = "invoices";
    public static final String PRODUCTS_FILE = "products.dat";
    public static final String CUSTOMERS_FILE = "customers.dat";
    public static final String ORDERS_FILE = "orders.dat";
    public static final String COUPONS_FILE = "coupons.dat";
    public static final String WISHLIST_FILE = "wishlist.dat";

    // Concurrency Constants
    public static final String THREAD_WORKER_PREFIX = "Maisonelle-Worker-";
    public static final String DAEMON_MONITOR_NAME = "Maisonelle-Inventory-Monitor";
    public static final long INVENTORY_MONITOR_INTERVAL_MS = 15000L; // 15 seconds
}
