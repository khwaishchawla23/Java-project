package com.maisonelle.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a completed customer purchase with financial line items,
 * fulfillment status, and invoice reference.
 */
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status {
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    private final String orderId;
    private final int customerId;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final String shippingAddress;
    private final LocalDateTime orderDate;
    private final List<CartItem> items;
    private final BigDecimal subtotal;
    private final String couponCode;
    private final BigDecimal discountAmount;
    private final BigDecimal gstAmount;
    private final BigDecimal deliveryFee;
    private final BigDecimal totalAmount;
    private final String paymentMethod;
    private Status status;
    private String invoiceFilePath;

    public Order(String orderId, int customerId, String customerName, String customerEmail,
                 String customerPhone, String shippingAddress, List<CartItem> items,
                 BigDecimal subtotal, String couponCode, BigDecimal discountAmount,
                 BigDecimal gstAmount, BigDecimal deliveryFee, BigDecimal totalAmount,
                 String paymentMethod) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal != null ? subtotal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.couponCode = couponCode != null ? couponCode : "None";
        this.discountAmount = discountAmount != null ? discountAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.gstAmount = gstAmount != null ? gstAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.deliveryFee = deliveryFee != null ? deliveryFee.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash on Delivery";
        this.status = Status.CONFIRMED;
    }

    public int getTotalItemQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getFormattedDate() {
        return orderDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getCouponCode() { return couponCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getInvoiceFilePath() { return invoiceFilePath; }
    public void setInvoiceFilePath(String invoiceFilePath) { this.invoiceFilePath = invoiceFilePath; }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + status + " (₹" + totalAmount + ")";
    }
}
