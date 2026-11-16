# Maisonelle – Smart Boutique Desktop Shopping Application
> *“A little luxury, thoughtfully curated.”*

Maisonelle is a functional, standalone desktop e-commerce shopping platform developed exclusively in **Java 21** using **Java Swing & AWT** and standard JDK libraries.

Developed as a college-level Advanced Java project for second-year B.Tech CSE, Maisonelle demonstrates robust Object-Oriented Programming (OOP), modular software architecture, cryptography, and persistent file serialization without requiring any third-party frameworks, build tools, or databases.

---

## 1. Strict Technology & Architecture Constraints

| Technology | Role |
|---|---|
| **Java 21 (OpenJDK)** | Core language, Lambdas, Streams, Record/Sealed concepts |
| **Java Swing & AWT** | Custom painted GUI, CardLayout navigation, bicubic image rendering |
| **Java Collections Framework** | `ArrayList`, `HashMap`, `LinkedHashSet` for in-memory state |
| **Java Object Serialization** | Local binary preservation (`products.dat`, `customers.dat`, etc.) |
| **Java File Handling** | Plain text formatted invoice generation (`.txt`) |
| **Java Cryptography API** | `PBKDF2WithHmacSHA256` password hashing with unique per-user salts |
| **BigDecimal & RoundingMode** | Precision monetary calculations (subtotal, discounts, 18% GST) |

### Strictly Excluded
- ❌ No HTML / CSS / JavaScript / React / Node.js
- ❌ No MySQL / SQLite / JDBC / Spring Boot / Hibernate
- ❌ No Maven / Gradle external dependencies
- ❌ No FlatLaf / Apache PDFBox (100% Standard Java JDK only)

---

## 2. Pastel Brown, Latte & Mocha Aesthetic

The application replaces harsh primary colors with a warm, editorial, boutique visual identity:

| Element | Palette Name | Hex Code | Swatch |
|---|---|---|---|
| **Main Background** | Vanilla Cream | `#F7F2EA` | Warm cream canvas |
| **Secondary Panels** | Soft Beige | `#EDE2D3` | Sidebar and badge fills |
| **Product Cards** | Warm Ivory | `#FFFCF8` | Elevated card surfaces |
| **Soft Accents** | Oat Latte | `#D8C3AD` | Highlight pills & active tabs |
| **Secondary Accent** | Dusty Taupe | `#C5AD98` | Borders & subtle lines |
| **Primary Buttons** | Mocha Brown | `#B99B80` | Call-to-action buttons |
| **Main Headings** | Cocoa Brown | `#8C6D56` | Section headers |
| **Dark Body Text** | Espresso | `#594438` | Readable contrast text |
| **Card Borders** | Sand Beige | `#E4D7C8` | Subtle containment outlines |
| **Muted Metadata** | Warm Grey Brown | `#9B8878` | Subtitles and captions |

---

## 3. Key Application Features

### 🔐 1. Authentication & Cryptographic Security
- **Salted PBKDF2 Password Hashing**: Passwords are never saved in plain text. A cryptographically random salt (`SecureRandom`) is generated per user and hashed with 65,536 iterations.
- **Role-Based Access Control**: Strict segregation between `CUSTOMER` and `ADMIN` privileges.
- **Demo Quick Access**: One-click demo credential pills for fast viva evaluation.

### 🛍️ 2. Product Catalogue & Real Photography
- **46+ Boutique Products** across 5 distinct departments:
  - Electronics (Headphones, speakers, smartwatches, keyboards, mice, power banks)
  - Fashion (Sneakers, boots, hoodies, tees, leather jackets, denim, backpacks)
  - Home & Living (Ceramic lamps, desk organizers, artisan mugs, diffusers)
  - Accessories (Briefcases, sunglasses, wallets, cases, cables)
  - Beauty & Lifestyle (Skincare sets, artisanal perfumes, yoga mats, silk masks)
- **ImageIO Bicubic Scaling**: Product photography loaded from local storage and scaled smoothly with aspect ratio preservation.
- **AWT Fallback Generator**: Missing images are gracefully replaced with a monogrammed pastel card.

### 🛒 3. Interactive Shopping Bag
- Add to bag with strict stock enforcement.
- Real-time quantity adjustments (`+` / `−`).
- Live subtotal, estimated GST, and delivery calculations.
- Seamless bag clearing and checkout handoff.

### 🤍 4. Saved Wishlist
- One-click heart toggle on product cards and specification sheets.
- Bookmark favorite items without cluttering the cart.
- One-click transfer to active cart (`Move to Bag`).

### 💳 5. Checkout, Billing & Coupons
- Validates delivery recipient, address, city, and PIN code.
- **Voucher Codes**:
  - `WELCOME10`: 10% discount on orders above ₹1,000 (Max ₹500)
  - `SAVE20`: 20% discount on orders above ₹2,500 (Max ₹1,000)
  - `FLAT100`: Flat ₹100 instant discount on orders above ₹500
- **Statutory 18% GST** calculated with `BigDecimal`.
- **Delivery Fee**: Free on taxable orders above ₹1,000; otherwise ₹99.

### 📄 6. Clean Text Invoice Generation
- Generates formatted, human-readable `.txt` invoices directly to the `invoices/` directory.
- Opens automatically in the system default text editor upon placement or history inspection.

### 📊 7. Administrator Control Center
- **Real-time KPI metrics**: Sales revenue, total orders, active catalog pieces, and low-stock alerts.
- **Catalogue CRUD**: Add new pieces, update pricing, restock inventory, or delete discontinued items.
- **Live Search & Filter**: Rapidly filter 46+ products by name, brand, or department.
- **Order Fulfillment**: Track and update fulfillment status (`CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
- **Custom Pure AWT Analytics Chart**: Revenue-by-category bar chart painted with zero external libraries.

---

## 4. Package & Directory Structure

```
Maisonelle/
├── src/
│   └── com/
│       └── maisonelle/
│           ├── Main.java                        # Application Entry Point
│           ├── model/
│           │   ├── Product.java                 # Catalogue Item Model
│           │   ├── Customer.java                # User Profile & Roles
│           │   ├── Cart.java                    # Shopping Bag Container
│           │   ├── CartItem.java                # Line Item with Quantity
│           │   ├── Order.java                   # Order & Status Entity
│           │   └── Coupon.java                  # Promotional Voucher Model
│           ├── manager/
│           │   ├── ProductManager.java          # Product Catalog Operations
│           │   ├── UserManager.java             # User Authentication & State
│           │   ├── CartManager.java             # Active Cart Business Logic
│           │   ├── WishlistManager.java         # Wishlist Bookmarks
│           │   └── OrderManager.java            # Order Placement & Deduction
│           ├── service/
│           │   ├── BillingService.java          # Subtotal, GST & Delivery
│           │   └── DiscountService.java         # Coupon Validation Logic
│           ├── ui/
│           │   ├── LoginFrame.java              # Sign In Screen
│           │   ├── RegisterFrame.java           # Registration Screen
│           │   ├── HomeFrame.java               # CardLayout Dashboard
│           │   ├── ProductPanel.java            # Product Grid & Filters
│           │   ├── ProductDetailsPanel.java     # Detailed Product View
│           │   ├── CartPanel.java               # Shopping Bag View
│           │   ├── WishlistPanel.java           # Saved Wishlist View
│           │   ├── CheckoutPanel.java           # Address & Payment
│           │   ├── OrderHistoryPanel.java       # Past Orders & Receipts
│           │   └── AdminPanel.java              # Administrator Dashboard
│           └── util/
│               ├── DataManager.java             # Serialization Persistence
│               ├── PasswordUtil.java            # PBKDF2 HMAC-SHA256 Security
│               ├── Theme.java                   # Colors, Fonts & UI Factory
│               ├── ImageUtils.java              # ImageIO Loading & Scaling
│               └── InvoiceGenerator.java        # Formatted .txt Invoices
├── resources/
│   └── images/                                  # 46 Genuine Product Images
├── data/                                        # Serialized .dat Storage
├── invoices/                                    # Generated Order Invoices
├── test/
│   └── com/
│       └── maisonelle/
│           └── MaisonelleTestRunner.java        # Automated 48-Test Suite
├── build.sh                                     # Terminal Build Script
├── run.sh                                       # Terminal Execution Script
├── Maisonelle.command                           # 1-Click macOS Double-Click
└── README.md
```

---

## 5. Quick Start Instructions

### Prerequisites
- Java 21+ JDK (Oracle OpenJDK 21/27 or Eclipse Temurin 21)

### Option A: 1-Click Launch (macOS)
Double-click `Maisonelle.command` inside the `Maisonelle` folder.

### Option B: Terminal Command
```bash
cd /Users/khwaishchawla/Maisonelle

# 1. Compile all source files
./build.sh

# 2. Run the application
./run.sh
```

### Option C: Run Automated Test Suite
```bash
javac -d bin -sourcepath src:test test/com/maisonelle/MaisonelleTestRunner.java
java -cp "bin:resources" com.maisonelle.MaisonelleTestRunner
```

---

## 6. Default Demonstration Accounts

| Role | Email | Password | Privileges |
|---|---|---|---|
| **Administrator** | `admin@maisonelle.com` | `admin123` | Full Admin Dashboard, stock & price editing, order fulfillment |
| **Customer** | `clara@maisonelle.com` | `customer123` | Store browsing, cart, wishlist, checkout, order history |
