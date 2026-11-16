package com.maisonelle.util;

import com.maisonelle.model.Coupon;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Order;
import com.maisonelle.model.Product;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Handles persistent data storage via Java Object Serialization and File I/O.
 * Manages initialization of seed data, directory setup, and data migration.
 * Strictly standard Java 21 only.
 */
public class DataManager {

    private static final String APP_DIR_NAME = ".maisonelle";
    private static final String DATA_DIR_NAME = "data";
    private static final String INVOICE_DIR_NAME = "invoices";

    private static final String PRODUCTS_FILE = "products.dat";
    private static final String CUSTOMERS_FILE = "customers.dat";
    private static final String ORDERS_FILE = "orders.dat";
    private static final String COUPONS_FILE = "coupons.dat";
    private static final String WISHLIST_FILE = "wishlist.dat";

    private final File dataDir;
    private final File invoiceDir;

    public DataManager() {
        // Prefer local project directories if present, otherwise fallback to user home ~/.maisonelle/
        File localData = new File("data");
        File localInvoices = new File("invoices");

        if (localData.exists() || localData.mkdirs()) {
            this.dataDir = localData;
        } else {
            File homeAppDir = new File(System.getProperty("user.home", "."), APP_DIR_NAME);
            this.dataDir = new File(homeAppDir, DATA_DIR_NAME);
            this.dataDir.mkdirs();
        }

        if (localInvoices.exists() || localInvoices.mkdirs()) {
            this.invoiceDir = localInvoices;
        } else {
            File homeAppDir = new File(System.getProperty("user.home", "."), APP_DIR_NAME);
            this.invoiceDir = new File(homeAppDir, INVOICE_DIR_NAME);
            this.invoiceDir.mkdirs();
        }
    }

    public File getDataDir() { return dataDir; }
    public File getInvoiceDir() { return invoiceDir; }

    // ==================== PRODUCTS ====================

    @SuppressWarnings("unchecked")
    public synchronized ArrayList<Product> loadProducts() {
        File file = new File(dataDir, PRODUCTS_FILE);
        if (!file.exists()) {
            ArrayList<Product> initial = createInitialProducts();
            saveProducts(initial);
            return initial;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Product> loaded = (ArrayList<Product>) ois.readObject();
            if (loaded == null || loaded.size() < 40) {
                ArrayList<Product> refreshed = createInitialProducts();
                saveProducts(refreshed);
                return refreshed;
            }
            return loaded;
        } catch (Exception e) {
            System.err.println("Notice: Reinitializing products catalog: " + e.getMessage());
            ArrayList<Product> fallback = createInitialProducts();
            saveProducts(fallback);
            return fallback;
        }
    }

    public synchronized void saveProducts(List<Product> products) {
        File file = new File(dataDir, PRODUCTS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(products));
        } catch (IOException e) {
            System.err.println("Failed to save products: " + e.getMessage());
        }
    }

    // ==================== CUSTOMERS ====================

    @SuppressWarnings("unchecked")
    public synchronized HashMap<Integer, Customer> loadCustomers() {
        File file = new File(dataDir, CUSTOMERS_FILE);
        if (!file.exists()) {
            HashMap<Integer, Customer> initial = createInitialCustomers();
            saveCustomers(initial);
            return initial;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            HashMap<Integer, Customer> loaded = (HashMap<Integer, Customer>) ois.readObject();
            if (loaded == null || loaded.isEmpty()) {
                HashMap<Integer, Customer> initial = createInitialCustomers();
                saveCustomers(initial);
                return initial;
            }
            return loaded;
        } catch (Exception e) {
            System.err.println("Notice: Reinitializing customers: " + e.getMessage());
            HashMap<Integer, Customer> fallback = createInitialCustomers();
            saveCustomers(fallback);
            return fallback;
        }
    }

    public synchronized void saveCustomers(Map<Integer, Customer> customers) {
        File file = new File(dataDir, CUSTOMERS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new HashMap<>(customers));
        } catch (IOException e) {
            System.err.println("Failed to save customers: " + e.getMessage());
        }
    }

    // ==================== ORDERS ====================

    @SuppressWarnings("unchecked")
    public synchronized ArrayList<Order> loadOrders() {
        File file = new File(dataDir, ORDERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Order> loaded = (ArrayList<Order>) ois.readObject();
            return loaded != null ? loaded : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Notice: Initializing empty orders history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized void saveOrders(List<Order> orders) {
        File file = new File(dataDir, ORDERS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(orders));
        } catch (IOException e) {
            System.err.println("Failed to save orders: " + e.getMessage());
        }
    }

    // ==================== COUPONS ====================

    @SuppressWarnings("unchecked")
    public synchronized ArrayList<Coupon> loadCoupons() {
        File file = new File(dataDir, COUPONS_FILE);
        if (!file.exists()) {
            ArrayList<Coupon> initial = createInitialCoupons();
            saveCoupons(initial);
            return initial;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Coupon> loaded = (ArrayList<Coupon>) ois.readObject();
            return loaded != null ? loaded : createInitialCoupons();
        } catch (Exception e) {
            System.err.println("Notice: Reinitializing coupons: " + e.getMessage());
            ArrayList<Coupon> fallback = createInitialCoupons();
            saveCoupons(fallback);
            return fallback;
        }
    }

    public synchronized void saveCoupons(List<Coupon> coupons) {
        File file = new File(dataDir, COUPONS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(coupons));
        } catch (IOException e) {
            System.err.println("Failed to save coupons: " + e.getMessage());
        }
    }

    // ==================== WISHLIST ====================

    @SuppressWarnings("unchecked")
    public synchronized LinkedHashSet<Integer> loadWishlist() {
        File file = new File(dataDir, WISHLIST_FILE);
        if (!file.exists()) {
            return new LinkedHashSet<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            LinkedHashSet<Integer> loaded = (LinkedHashSet<Integer>) ois.readObject();
            return loaded != null ? loaded : new LinkedHashSet<>();
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    public synchronized void saveWishlist(Set<Integer> productIds) {
        File file = new File(dataDir, WISHLIST_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new LinkedHashSet<>(productIds));
        } catch (IOException e) {
            System.err.println("Failed to save wishlist: " + e.getMessage());
        }
    }

    // ==================== INITIAL SEED DATA ====================

    private HashMap<Integer, Customer> createInitialCustomers() {
        HashMap<Integer, Customer> map = new HashMap<>();

        // 1. Initial Administrator Account
        String adminSalt = PasswordUtil.generateSalt();
        String adminHash = PasswordUtil.hashPassword("admin123", adminSalt);
        Customer admin = new Customer(1, "Maisonelle Administrator", "admin@maisonelle.com",
                "+91 98765 43210", "12 Rue de la Paix", "Mumbai", "400001",
                adminHash, adminSalt, Customer.Role.ADMIN);
        map.put(admin.getId(), admin);

        // 2. Initial Sample Customer Account
        String custSalt = PasswordUtil.generateSalt();
        String custHash = PasswordUtil.hashPassword("customer123", custSalt);
        Customer clara = new Customer(2, "Clara Bennett", "clara@maisonelle.com",
                "+91 98200 12345", "45 Green Park Avenue", "Bengaluru", "560001",
                custHash, custSalt, Customer.Role.CUSTOMER);
        map.put(clara.getId(), clara);

        return map;
    }

    private ArrayList<Coupon> createInitialCoupons() {
        ArrayList<Coupon> list = new ArrayList<>();
        list.add(new Coupon("WELCOME10", Coupon.Type.PERCENTAGE, new BigDecimal("10.00"),
                new BigDecimal("1000.00"), new BigDecimal("500.00"), "10% off on boutique orders above ₹1,000", true));
        list.add(new Coupon("SAVE20", Coupon.Type.PERCENTAGE, new BigDecimal("20.00"),
                new BigDecimal("2500.00"), new BigDecimal("1000.00"), "20% luxury discount on orders above ₹2,500", true));
        list.add(new Coupon("FLAT100", Coupon.Type.FLAT, new BigDecimal("100.00"),
                new BigDecimal("500.00"), new BigDecimal("100.00"), "Flat ₹100 instant savings on orders above ₹500", true));
        return list;
    }

    private ArrayList<Product> createInitialProducts() {
        ArrayList<Product> list = new ArrayList<>();

        // --- ELECTRONICS (10 Products) ---
        list.add(new Product(101, "Sony WH-1000XM5 Studio Headphones", "Electronics",
                "Flagship noise-canceling headphones crafted with carbon fiber drivers and ultra-soft vegan leather.",
                new BigDecimal("29990.00"), new BigDecimal("34990.00"), 18, "sony_headphones.jpg", 4.9, "Sony", true));

        list.add(new Product(102, "JBL Flip 6 Portable Bluetooth Speaker", "Electronics",
                "Rich 2-way acoustic system engineered with IP67 water resistance and 12-hour continuous battery life.",
                new BigDecimal("9999.00"), new BigDecimal("13999.00"), 25, "jbl_speaker.jpg", 4.7, "JBL", true));

        list.add(new Product(103, "Apple Watch Series 9 GPS 45mm", "Electronics",
                "Advanced health sensors, Always-On Retina OLED display, and seamless aluminum unibody styling.",
                new BigDecimal("41900.00"), new BigDecimal("44900.00"), 14, "apple_watch.jpg", 4.8, "Apple", true));

        list.add(new Product(104, "Keychron K2 V2 Wireless Keyboard", "Electronics",
                "Minimal tactile mechanical switches with warm backlight, Mac/Windows keycaps, and aluminum bezel.",
                new BigDecimal("7499.00"), new BigDecimal("9999.00"), 16, "mechanical_keyboard.jpg", 4.6, "Keychron", false));

        list.add(new Product(105, "Logitech MX Master 3S Precision Mouse", "Electronics",
                "Electromagnetic MagSpeed scroll wheel, 8K DPI glass sensor, and quiet tactile clicking experience.",
                new BigDecimal("8995.00"), new BigDecimal("10995.00"), 28, "logitech_mouse.jpg", 4.9, "Logitech", true));

        list.add(new Product(106, "Razer DeathAdder V3 Pro Wireless Mouse", "Electronics",
                "63-gram lightweight design with Focus Pro 30K optical sensor and hyper-responsive optical switches.",
                new BigDecimal("11999.00"), new BigDecimal("14999.00"), 15, "gaming_mouse.jpg", 4.6, "Razer", false));

        list.add(new Product(107, "Anker 737 Ultra Power Bank (24K)", "Electronics",
                "140W dual-direction Power Delivery with integrated digital status screen and GaNPrime safety.",
                new BigDecimal("12499.00"), new BigDecimal("15999.00"), 22, "power_bank.jpg", 4.7, "Anker", true));

        list.add(new Product(108, "Anker 8-in-1 Aluminum USB-C Hub", "Electronics",
                "Precision machined aluminum chassis with 100W PD charging, 4K@60Hz HDMI, and high-speed card readers.",
                new BigDecimal("4499.00"), new BigDecimal("5999.00"), 35, "usbc_hub.jpg", 4.5, "Anker", false));

        list.add(new Product(109, "boAt Airdopes 141 ANC Wireless Earbuds", "Electronics",
                "Active noise cancellation, 42 hours total playtime, low latency beast mode, and fast Type-C charging.",
                new BigDecimal("1499.00"), new BigDecimal("3990.00"), 50, "boat_earbuds.jpg", 4.4, "boAt", false));

        list.add(new Product(110, "Besign LSX6 Aluminum Laptop Stand", "Electronics",
                "Ergonomic multi-angle riser built with sandblasted anodized aluminum for improved desk posture.",
                new BigDecimal("1699.00"), new BigDecimal("2499.00"), 40, "laptop_stand.jpg", 4.6, "Besign", false));

        // --- FASHION (10 Products) ---
        list.add(new Product(201, "Nike Air Max Casual Sneakers", "Fashion",
                "Signature visible Air cushioning, stitched leather overlays, and refined retro runner silhouette.",
                new BigDecimal("8295.00"), new BigDecimal("10295.00"), 20, "nike_sneakers.jpg", 4.8, "Nike", true));

        list.add(new Product(202, "Minimalist Suede Casual Shoes", "Fashion",
                "Handcrafted Italian-style suede loafers with cushioned memory foam insole and flexible rubber outsole.",
                new BigDecimal("3499.00"), new BigDecimal("5999.00"), 24, "casual_shoes.jpg", 4.5, "Maisonelle", false));

        list.add(new Product(203, "Champion Reverse Weave Heavy Hoodie", "Fashion",
                "Dense fleece construction with signature ribbed side gussets and timeless relaxed unisex drape.",
                new BigDecimal("4499.00"), new BigDecimal("6499.00"), 26, "champion_hoodie.jpg", 4.7, "Champion", true));

        list.add(new Product(204, "Levi's Classic Crewneck Tee (Pure Cotton)", "Fashion",
                "100% breathable organic combed cotton featuring durable double-stitched hems and soft hand feel.",
                new BigDecimal("1299.00"), new BigDecimal("1799.00"), 45, "levis_tshirt.jpg", 4.5, "Levi's", false));

        list.add(new Product(205, "Lambskin Moto Biker Jacket", "Fashion",
                "Supple genuine full-grain leather tailored with asymmetrical metal zipper and quilted satin lining.",
                new BigDecimal("14999.00"), new BigDecimal("21999.00"), 10, "biker_jacket.jpg", 4.9, "Maisonelle", true));

        list.add(new Product(206, "Levi's 511 Slim Fit Stretch Denim", "Fashion",
                "Modern slim silhouette with built-in stretch flexibility and authentic whiskered indigo wash.",
                new BigDecimal("2999.00"), new BigDecimal("4199.00"), 32, "levis_jeans.jpg", 4.6, "Levi's", false));

        list.add(new Product(207, "Nordic Commuter Travel Backpack (28L)", "Fashion",
                "Weatherproof matte nylon fabric with dedicated 16-inch fleece laptop sleeve and luggage pass-through.",
                new BigDecimal("4299.00"), new BigDecimal("6999.00"), 25, "travel_backpack.jpg", 4.7, "Maisonelle", true));

        list.add(new Product(208, "Fossil Grant Chronograph Leather Watch", "Fashion",
                "Rich cream sunray dial paired with Roman numeral hour markers and vintage brown leather strap.",
                new BigDecimal("9495.00"), new BigDecimal("12995.00"), 18, "fossil_watch.jpg", 4.7, "Fossil", false));

        list.add(new Product(209, "Titan Neo Minimal Slim Men's Watch", "Fashion",
                "Slimline stainless steel case with mineral crystal glass and elegant midnight indices.",
                new BigDecimal("4995.00"), new BigDecimal("6495.00"), 22, "titan_watch.jpg", 4.5, "Titan", false));

        list.add(new Product(210, "Samsung 32-inch 4K Curved Smart Monitor", "Electronics",
                "1500R curved UHD panel featuring HDR10, integrated stereo speakers, and USB-C connectivity.",
                new BigDecimal("27999.00"), new BigDecimal("35999.00"), 12, "monitor.jpg", 4.8, "Samsung", true));

        // --- HOME & LIVING (10 Products) ---
        list.add(new Product(301, "Nordic Ceramic Dimmable Table Lamp", "Home & Living",
                "Fluted matte ceramic base with warm woven linen drum shade and touch-sensitive dimming.",
                new BigDecimal("3499.00"), new BigDecimal("4999.00"), 15, "table_lamp.jpg", 4.8, "Maisonelle", true));

        list.add(new Product(302, "Walnut & Brass Modular Desk Organizer", "Home & Living",
                "Solid American walnut tray with satin brass stationery divider and felt-lined compartments.",
                new BigDecimal("2199.00"), new BigDecimal("3299.00"), 28, "desk_organizer.jpg", 4.7, "Maisonelle", false));

        list.add(new Product(303, "Ceramic Artisan Matte Coffee Mug (380ml)", "Home & Living",
                "Individually hand-glazed stoneware mug with comfortable wide-loop handle for cozy latte moments.",
                new BigDecimal("699.00"), new BigDecimal("1199.00"), 60, "coffee_mug.jpg", 4.6, "Maisonelle", false));

        list.add(new Product(304, "Hydro Flask 24oz Insulated Bottle", "Home & Living",
                "TempShield double-wall vacuum insulation keeps cold beverages iced up to 24 hours.",
                new BigDecimal("2799.00"), new BigDecimal("3499.00"), 35, "hydro_flask.jpg", 4.8, "Hydro Flask", true));

        list.add(new Product(305, "Stanley Quencher H2.0 FlowState (40oz)", "Home & Living",
                "Recycled stainless steel tumbler with ergonomic handle, comfort-grip insert, and reusable straw.",
                new BigDecimal("3999.00"), new BigDecimal("4999.00"), 25, "stanley_tumbler.jpg", 4.9, "Stanley", true));

        list.add(new Product(306, "100% Egyptian Cotton Luxury Bed Sheet Set", "Home & Living",
                "Silky 400-thread-count sateen weave sheet set with deep pocket fitted sheet and pillowcases.",
                new BigDecimal("4499.00"), new BigDecimal("6999.00"), 20, "bedsheet.jpg", 4.8, "Maisonelle", false));

        list.add(new Product(307, "Woven Linen Collapsible Storage Cube", "Home & Living",
                "Reinforced structure covered with heavyweight natural linen and vegan leather pull handles.",
                new BigDecimal("1299.00"), new BigDecimal("1899.00"), 40, "storage_box.jpg", 4.4, "Maisonelle", false));

        list.add(new Product(308, "Minimalist Silent Sweep Wood Wall Clock", "Home & Living",
                "Natural beech wood frame with ultra-quiet quartz sweep movement and clean Scandinavian indices.",
                new BigDecimal("1899.00"), new BigDecimal("2799.00"), 30, "wall_clock.jpg", 4.6, "Maisonelle", false));

        list.add(new Product(309, "Ultrasonic Essential Oil Ceramic Diffuser", "Home & Living",
                "Whisper-quiet ceramic aromatherapy diffuser with ambient warm LED nightlight and timer modes.",
                new BigDecimal("2499.00"), new BigDecimal("3799.00"), 22, "aroma_diffuser.jpg", 4.7, "Maisonelle", true));

        list.add(new Product(310, "Boutique Stainless Steel Water Bottle (750ml)", "Home & Living",
                "Powder-coated food-grade steel flask engineered with leakproof bamboo-inlaid loop cap.",
                new BigDecimal("1199.00"), new BigDecimal("1699.00"), 45, "water_bottle.jpg", 4.5, "Maisonelle", false));

        // --- ACCESSORIES (9 Products) ---
        list.add(new Product(401, "Full Grain Leather Executive Briefcase", "Accessories",
                "Rich vegetable-tanned oil pull-up leather with dedicated 15.6-inch laptop pocket and brass hardware.",
                new BigDecimal("6999.00"), new BigDecimal("9999.00"), 18, "laptop_bag.jpg", 4.9, "Maisonelle", true));

        list.add(new Product(402, "Ray-Ban Classic Aviator Polarized Sunglasses", "Accessories",
                "Gold metal frame with G-15 crystal green polarized lenses offering 100% UV400 radiation protection.",
                new BigDecimal("8590.00"), new BigDecimal("10890.00"), 20, "rayban_aviator.jpg", 4.8, "Ray-Ban", true));

        list.add(new Product(403, "Classic Square Acetate Sunglasses", "Accessories",
                "Handcrafted Italian cellulose acetate frames equipped with anti-reflective scratch-resistant lenses.",
                new BigDecimal("2199.00"), new BigDecimal("3499.00"), 30, "square_sunglasses.jpg", 4.5, "Maisonelle", false));

        list.add(new Product(404, "Bifold RFID Blocking Genuine Leather Wallet", "Accessories",
                "Top-grain cowhide leather with 8 card slots, currency divider, and built-in RFID security shield.",
                new BigDecimal("1499.00"), new BigDecimal("2299.00"), 40, "leather_wallet.jpg", 4.7, "Maisonelle", false));

        list.add(new Product(405, "MagSafe Protective Vegan Leather Phone Case", "Accessories",
                "Precision molded with tactile aluminum buttons, raised camera bezel, and built-in N52 neodymium magnets.",
                new BigDecimal("1199.00"), new BigDecimal("1999.00"), 50, "phone_case.jpg", 4.6, "Maisonelle", false));

        list.add(new Product(406, "Braided Heavy-Duty Type-C Fast Cable (2M)", "Accessories",
                "Reinforced aramid fiber core with double-nylon braiding capable of 100W ultra-fast charging.",
                new BigDecimal("699.00"), new BigDecimal("1299.00"), 65, "charging_cable.jpg", 4.7, "Anker", false));

        list.add(new Product(407, "Solid Aluminum Adjustable Phone & Tablet Stand", "Accessories",
                "Dual hinge 270-degree rotation with anti-slip silicone cushions for hands-free video viewing.",
                new BigDecimal("899.00"), new BigDecimal("1499.00"), 45, "phone_stand.jpg", 4.6, "Besign", false));

        list.add(new Product(408, "Quilted Water-Resistant Tablet Sleeve (11-inch)", "Accessories",
                "Shockproof high-density foam padding encased in water-repellent micro-quilted fabric.",
                new BigDecimal("1299.00"), new BigDecimal("1899.00"), 35, "tablet_sleeve.jpg", 4.5, "Maisonelle", false));

        list.add(new Product(409, "Compression Luggage Packing Cubes (6-Pack)", "Accessories",
                "Ultra-lightweight ripstop nylon with double zipper compression system for clutter-free travel.",
                new BigDecimal("1799.00"), new BigDecimal("2699.00"), 30, "travel_cubes.jpg", 4.8, "Maisonelle", true));

        // --- BEAUTY & LIFESTYLE (8 Products) ---
        list.add(new Product(501, "Botanical Glow Facial Skincare Kit", "Beauty & Lifestyle",
                "Gentle cleansing mousse, niacinamide balancing toner, and hyaluronic moisture elixir cream.",
                new BigDecimal("2999.00"), new BigDecimal("4499.00"), 24, "skincare_kit.jpg", 4.8, "Maisonelle", true));

        list.add(new Product(502, "Maisonelle Artisanal Eau de Parfum Gift Set", "Beauty & Lifestyle",
                "Four curated 30ml extrait de parfum scents featuring notes of bergamot, cedarwood, and amber vanilla.",
                new BigDecimal("4999.00"), new BigDecimal("6999.00"), 16, "perfume_set.jpg", 4.9, "Maisonelle", true));

        list.add(new Product(503, "Gentleman's Matte Grooming & Shaving Kit", "Beauty & Lifestyle",
                "Stainless steel double-edge safety razor, badger-hair brush, and organic sandalwood shaving balm.",
                new BigDecimal("2499.00"), new BigDecimal("3599.00"), 25, "grooming_kit.jpg", 4.7, "Maisonelle", false));

        list.add(new Product(504, "Eco-Friendly High-Density TPE Yoga Mat (6mm)", "Beauty & Lifestyle",
                "Dual-tone alignment texture with superior anti-slip floor grip and convenient carry strap.",
                new BigDecimal("1899.00"), new BigDecimal("2899.00"), 32, "yoga_mat.jpg", 4.6, "Maisonelle", false));

        list.add(new Product(505, "Luxury Silk Sleep Mask & Pillowcase Set", "Beauty & Lifestyle",
                "Grade 6A 22-momme pure mulberry silk designed to hydrate hair and prevent delicate sleep creases.",
                new BigDecimal("2199.00"), new BigDecimal("3199.00"), 28, "beauty_box.jpg", 4.8, "Maisonelle", true));

        list.add(new Product(506, "Smart Fitness Tracker with AMOLED Display", "Beauty & Lifestyle",
                "Continuous SpO2 oxygen monitor, heart-rate analytics, sleep tracking, and 14-day battery reserve.",
                new BigDecimal("3499.00"), new BigDecimal("4999.00"), 25, "fitness_band.jpg", 4.6, "Fitbit", false));

        list.add(new Product(507, "Waterproof Vegan Leather Extended Desk Mat", "Home & Living",
                "Smooth micro-textured surface measuring 90x40cm for smooth mouse glides and desk protection.",
                new BigDecimal("1199.00"), new BigDecimal("1799.00"), 40, "desk_mat.jpg", 4.7, "Maisonelle", false));

        list.add(new Product(508, "Maisonelle Signature Ceramic Candle Gift Box", "Beauty & Lifestyle",
                "Hand-poured 100% natural soy wax candle scented with French lavender, fig, and creamy cashmere.",
                new BigDecimal("1599.00"), new BigDecimal("2299.00"), 30, "aroma_diffuser.jpg", 4.8, "Maisonelle", true));

        return list;
    }
}
