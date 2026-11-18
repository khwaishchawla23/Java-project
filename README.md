# Maisonelle – Terminal-Based Boutique E-Commerce Application
> *“A little luxury, thoughtfully curated.”*

Maisonelle is a 100% standalone **terminal-based e-commerce shopping platform** developed entirely in **Java 21** using standard built-in JDK libraries.

Developed as an Advanced Java college project for second-year B.Tech CSE, Maisonelle demonstrates robust Object-Oriented Programming (OOP), modular tiered architecture, cryptographic security, thread-safe collections, and binary object serialization without requiring any third-party frameworks, build tools, external databases, or graphical UI dependencies.

---

## 1. Strict Technology & Architecture Requirements

| Technology | Role in Maisonelle |
|---|---|
| **Java 21 (OpenJDK)** | Core language, Lambdas, Streams, Record-style immutability |
| **Java Console & Scanner** | Interactive menu loops, robust input sanitization, error recovery |
| **Java Collections Framework** | `ArrayList`, `HashMap`, `LinkedHashSet` for in-memory catalogue & state |
| **Java Object Serialization** | Persistent local binary storage (`products.dat`, `customers.dat`, etc.) |
| **Java File Handling (I/O)** | Tax invoice generation to text files (`invoices/Invoice_ORD-*.txt`) |
| **Java Cryptography API** | `PBKDF2WithHmacSHA256` password hashing with unique 16-byte cryptographic salts |
| **BigDecimal & RoundingMode** | High-precision monetary calculations (Subtotal, Discounts, 18% GST, Delivery) |

### Strictly Excluded
- ❌ No Swing / AWT GUI components / JavaFX
- ❌ No HTML / CSS / JavaScript / React / Node.js
- ❌ No MySQL / SQLite / JDBC / Spring Boot / Hibernate
- ❌ No Maven / Gradle external dependencies
- ❌ No FlatLaf / Apache PDFBox (100% Standard Java JDK only)

---

## 2. Console Architecture (`com.maisonelle.console`)

```
src/com/maisonelle/
├── Main.java                        # Terminal application entry point
├── console/
│   ├── ConsoleUI.java              # Master navigation controller & authentication flows
│   ├── CustomerMenu.java           # Complete customer e-commerce shopping workflows
│   ├── AdminMenu.java              # Back-office administrator control center & KPI dashboard
│   ├── InputHelper.java            # Robust Scanner input utility (prevents token errors/loops)
│   └── ConsoleFormatter.java        # ASCII borders, tabular printers, and Rupee formatters
├── manager/
│   ├── ProductManager.java         # Catalogue search, filter, sort, and stock CRUD
│   ├── UserManager.java            # PBKDF2 authentication, registration, session management
│   ├── CartManager.java            # In-memory shopping cart operations
│   ├── WishlistManager.java        # Saved favorite items and move-to-cart operations
│   └── OrderManager.java           # Order placement, inventory reduction, sales KPI metrics
├── model/
│   ├── Product.java                # Catalogue item entity (BigDecimal pricing, stock)
│   ├── Customer.java               # Customer profile with Role enum (CUSTOMER, ADMIN)
│   ├── Cart.java                   # Active shopping cart container
│   ├── CartItem.java               # Line item with product snapshot & quantity
│   ├── Coupon.java                 # Percentage & flat voucher logic
│   └── Order.java                  # Order entity with fulfillment Status enum
├── service/
│   ├── BillingService.java         # Subtotal, voucher deductions, 18% GST, delivery fees
│   └── DiscountService.java        # Voucher code validation & calculations
└── util/
    ├── DataManager.java            # Binary object serialization to data/*.dat
    ├── PasswordUtil.java           # Salted PBKDF2 cryptographic hashing
    └── InvoiceGenerator.java       # Human-readable text invoice generation & terminal preview
```

---

## 3. Key Features

### 👤 Customer Features
1. **Registration & Cryptographic Login**: New account registration with field validation; salted PBKDF2 password security.
2. **Browse Products Catalogue**: Paginated tabular display with Product ID, Name, Category, Price, Rating, and Stock.
3. **Keyword Search**: Instant multi-field search across product names, brands, and descriptions.
4. **Category Filtering**: Filter by departments (*Electronics, Fashion, Home & Living, Accessories, Beauty & Lifestyle*).
5. **Multi-Criteria Sorting**: Sort by *Price (Low to High)*, *Price (High to Low)*, *Top Rating*, or *Alphabetical (A-Z)*.
6. **Product Detail Cards**: View full specifications, original MRP, discount tag, stock count, and description.
7. **Interactive Cart**: Add items, update quantities with inventory limits, remove items, or clear cart.
8. **Wishlist**: Save favorite pieces; one-click move to shopping cart.
9. **Promotional Coupons**:
   - `WELCOME10`: 10% discount on orders above ₹1,000 (Max ₹500)
   - `SAVE20`: 20% discount on orders above ₹2,500 (Max ₹1,000)
   - `FLAT100`: Flat ₹100 instant discount on orders above ₹500
10. **Checkout & Billing**:
    - Itemized breakdown: Subtotal, Discount, Taxable Amount, 18% Statutory GST, Delivery Fee.
    - Free delivery on orders of ₹1,000 and above (otherwise ₹99).
    - Delivery address confirmation.
    - Payment mode selection (UPI, Card, Net Banking, COD).
    - Order confirmation, inventory deduction, and instant terminal invoice preview.
11. **Order History**: View all previous purchases and display invoice receipts anytime.

### 🛡️ Administrator Features
1. **Role-Based Access**: Restricted exclusively to administrator accounts (`Role.ADMIN`).
2. **Executive KPI Dashboard**: Live display of Gross Revenue, Total Orders Placed, Registered Customer Count, and Low Stock Alerts.
3. **Catalogue CRUD**:
   - Add new products with automatic ID assignment.
   - Edit product name, category, brand, and description.
   - Update product price (`BigDecimal`).
   - Restock / update inventory count.
   - Delete products.
4. **Order Management**: Inspect all customer orders and update status (`CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED` → `CANCELLED`).
5. **Low Stock Alerts**: Displays products with inventory below 5 units.
6. **Executive Sales Summary**: Total revenue, order count, and Average Order Value (AOV).

---

## 4. Quick Demo Personas

For instant viva testing without typing credentials manually, select menu option **`4. Quick Demo Login`**:

| Role | Name | Email | Password |
|---|---|---|---|
| **Customer** | Clara Bennett | `clara@maisonelle.com` | `customer123` |
| **Administrator** | Maisonelle Admin | `admin@maisonelle.com` | `admin123` |

---

## 5. Build & Execution

### Prerequisites
- macOS or Linux with **Java 21 OpenJDK** installed.
- No Maven, Gradle, or internet connection required.

### Compile
```bash
./build.sh
```

### Run
```bash
./run.sh
```
*Or double-click `Maisonelle.command` in macOS Finder.*

### Run Automated Test Suite
```bash
/opt/homebrew/opt/openjdk/bin/javac -d bin -sourcepath "src:test" test/com/maisonelle/MaisonelleTestRunner.java
/opt/homebrew/opt/openjdk/bin/java -cp bin com.maisonelle.MaisonelleTestRunner
```

---

## 6. OOP & Advanced Java Concepts for Viva / Examination

1. **Encapsulation**:
   - Private fields across all models (`Product`, `Customer`, `Order`, `CartItem`) with public getters, setters, and business boundary methods (`reduceStock()`, `addItem()`).
2. **Abstraction**:
   - Business services (`BillingService`, `DiscountService`, `InvoiceGenerator`) hide computational complexities from menu presentation layers.
3. **Inheritance & Enums**:
   - Strongly typed Java enums with encapsulated behavior: `Customer.Role` (`CUSTOMER`, `ADMIN`), `Order.Status` (`CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`), `Coupon.Type` (`PERCENTAGE`, `FLAT`).
4. **Polymorphism**:
   - Dynamic voucher discount calculation using polymorphism and strategy patterns based on coupon types.
5. **Java Collections Framework**:
   - `ArrayList` for ordered catalogue, cart line items, and order history.
   - `HashMap<Integer, Customer>` for $O(1)$ customer lookup by ID.
   - `LinkedHashSet<Integer>` for wishlist management maintaining insertion order while preventing duplicates.
6. **Streams & Lambda Expressions**:
   - Category filtering (`stream().filter()`), keyword search (`contains()`), multi-field sorting (`sorted(Comparator)`), and aggregation (`mapToInt().sum()`).
7. **Exception Handling**:
   - Comprehensive error recovery using `try-catch-finally`, custom validation throwing `IllegalArgumentException` and `IllegalStateException`, and `InputHelper` preventing `NumberFormatException` and `InputMismatchException`.
8. **Thread-Safety & Concurrency**:
   - Synchronized critical sections across manager classes (`ProductManager`, `OrderManager`, `CartManager`) ensuring thread-safe inventory and state updates.
9. **Financial Precision with `BigDecimal`**:
   - Exact monetary mathematics using `BigDecimal` and `RoundingMode.HALF_UP` to prevent IEEE 754 floating-point inaccuracies during GST (18%) and voucher deduction calculations.
10. **Data Persistence via Serialization**:
    - Automatic loading and saving of application state to local binary files (`products.dat`, `customers.dat`, `orders.dat`, `wishlist.dat`, `coupons.dat`) using Java `ObjectOutputStream` and `ObjectInputStream`.
