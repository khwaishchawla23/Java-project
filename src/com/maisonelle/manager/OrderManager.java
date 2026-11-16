package com.maisonelle.manager;

import com.maisonelle.model.Cart;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Order;
import com.maisonelle.model.Product;
import com.maisonelle.service.BillingService;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Manages order creation, stock deductions, persistence, and status tracking.
 */
public class OrderManager {

    private final ArrayList<Order> orders;
    private final DataManager dataManager;
    private final ProductManager productManager;
    private final InvoiceGenerator invoiceGenerator;

    public OrderManager(DataManager dataManager, ProductManager productManager, InvoiceGenerator invoiceGenerator) {
        this.dataManager = dataManager;
        this.productManager = productManager;
        this.invoiceGenerator = invoiceGenerator;
        this.orders = dataManager.loadOrders();
    }

    public synchronized Order placeOrder(Customer customer, Cart cart, BillingService.BillingSummary billingSummary,
                                         String shippingAddress, String paymentMethod) throws Exception {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must be authenticated to place an order.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot place order with an empty shopping cart.");
        }

        // 1. Validate available stock for all line items
        for (CartItem item : cart.getItems()) {
            Product current = productManager.getProductById(item.getProduct().getId());
            if (current == null || current.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock available for: " +
                        (current != null ? current.getName() : "Product ID " + item.getProduct().getId()));
            }
        }

        // 2. Deduct inventory
        for (CartItem item : cart.getItems()) {
            Product p = productManager.getProductById(item.getProduct().getId());
            if (p != null) {
                p.reduceStock(item.getQuantity());
            }
        }
        dataManager.saveProducts(productManager.getAllProducts());

        // 3. Generate Unique Order ID (e.g. ORD-20261116-4821)
        String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int randomSuffix = 1000 + new Random().nextInt(9000);
        String orderId = "ORD-" + timestamp + "-" + randomSuffix;

        // 4. Create immutable line items snapshot
        List<CartItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product snapshot = new Product(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getCategory(),
                    item.getProduct().getDescription(),
                    item.getProduct().getPrice(),
                    item.getProduct().getOriginalPrice(),
                    item.getProduct().getStock(),
                    item.getProduct().getImagePath(),
                    item.getProduct().getRating(),
                    item.getProduct().getBrand(),
                    item.getProduct().isFeatured()
            );
            orderItems.add(new CartItem(snapshot, item.getQuantity()));
        }

        String finalAddress = shippingAddress != null && !shippingAddress.trim().isEmpty()
                ? shippingAddress.trim()
                : (customer.getAddress() + ", " + customer.getCity() + " - " + customer.getPincode());

        Order newOrder = new Order(
                orderId,
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                finalAddress,
                orderItems,
                billingSummary.getSubtotal(),
                billingSummary.getCouponCode(),
                billingSummary.getDiscountAmount(),
                billingSummary.getGstAmount(),
                billingSummary.getDeliveryFee(),
                billingSummary.getFinalPayableAmount(),
                paymentMethod
        );

        // 5. Generate Text Invoice
        try {
            File invoiceFile = invoiceGenerator.generateInvoiceFile(newOrder);
            newOrder.setInvoiceFilePath(invoiceFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Warning: Failed to generate invoice file: " + e.getMessage());
        }

        orders.add(0, newOrder); // Newest first
        dataManager.saveOrders(orders);

        return newOrder;
    }

    public synchronized List<Order> getOrdersForCustomer(int customerId) {
        return orders.stream()
                .filter(o -> o.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public synchronized List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public synchronized Order getOrderById(String orderId) {
        for (Order o : orders) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        return null;
    }

    public synchronized boolean updateOrderStatus(String orderId, Order.Status newStatus) {
        Order o = getOrderById(orderId);
        if (o != null) {
            o.setStatus(newStatus);
            dataManager.saveOrders(orders);
            return true;
        }
        return false;
    }

    public synchronized BigDecimal getTotalSales() {
        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (o.getStatus() != Order.Status.CANCELLED && o.getTotalAmount() != null) {
                total = total.add(o.getTotalAmount());
            }
        }
        return total;
    }

    public synchronized int getTotalOrdersCount() {
        return orders.size();
    }
}
