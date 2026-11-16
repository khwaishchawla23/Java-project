package com.maisonelle.ui;

import com.maisonelle.manager.CartManager;
import com.maisonelle.manager.OrderManager;
import com.maisonelle.manager.UserManager;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Order;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.math.BigDecimal;

/**
 * Complete Checkout experience with address input, demo payment selection,
 * coupon code redemption, and order finalization.
 */
public class CheckoutPanel extends JPanel {

    private final CartManager cartManager;
    private final OrderManager orderManager;
    private final UserManager userManager;
    private final BillingService billingService;
    private final DiscountService discountService;
    private final HomeFrame homeFrame;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField pinField;

    private JComboBox<String> paymentMethodCombo;
    private JTextField couponField;
    private JLabel couponFeedbackLabel;
    private String appliedCouponCode = null;

    private JPanel orderItemsSummaryContainer;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel gstLabel;
    private JLabel deliveryLabel;
    private JLabel totalLabel;

    public CheckoutPanel(CartManager cartManager, OrderManager orderManager, UserManager userManager,
                         BillingService billingService, DiscountService discountService, HomeFrame homeFrame) {
        this.cartManager = cartManager;
        this.orderManager = orderManager;
        this.userManager = userManager;
        this.billingService = billingService;
        this.discountService = discountService;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 32, 14, 32));

        JButton backBtn = Theme.createSecondaryButton("← Back to Bag");
        backBtn.addActionListener(e -> {
            if (homeFrame != null) homeFrame.showCart();
        });
        topBar.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Secure Boutique Checkout", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        topBar.add(title, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // Center Split View
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 32, 0));
        centerGrid.setOpaque(false);
        centerGrid.setBorder(new EmptyBorder(10, 32, 30, 32));

        // LEFT: Shipping & Payment Details
        centerGrid.add(createShippingAndPaymentCard());

        // RIGHT: Order Summary & Placement
        centerGrid.add(createOrderSummaryCard());

        JScrollPane scrollPane = new JScrollPane(centerGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        recalculateSummary();
    }

    private JPanel createShippingAndPaymentCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("1. Delivery Information");
        h1.setFont(Theme.FONT_TITLE);
        h1.setForeground(Theme.DARK_TEXT);
        card.add(h1);

        card.add(Box.createVerticalStrut(14));

        Customer current = userManager.getCurrentUser();
        nameField = Theme.createTextField(20);
        if (current != null) nameField.setText(current.getFullName());

        phoneField = Theme.createTextField(20);
        if (current != null) phoneField.setText(current.getPhone());

        addressField = Theme.createTextField(20);
        if (current != null && current.getAddress() != null) addressField.setText(current.getAddress());

        cityField = Theme.createTextField(20);
        if (current != null && current.getCity() != null) cityField.setText(current.getCity());

        pinField = Theme.createTextField(20);
        if (current != null && current.getPincode() != null) pinField.setText(current.getPincode());

        addFormField(card, "Recipient Name", nameField);
        addFormField(card, "Contact Phone Number", phoneField);
        addFormField(card, "Street Address", addressField);

        JPanel cityPinRow = new JPanel(new GridLayout(1, 2, 10, 0));
        cityPinRow.setOpaque(false);
        JPanel cCol = new JPanel(); cCol.setLayout(new BoxLayout(cCol, BoxLayout.Y_AXIS)); cCol.setOpaque(false);
        JLabel cLbl = new JLabel("City"); cLbl.setFont(Theme.FONT_SMALL_BOLD); cLbl.setForeground(Theme.DARK_TEXT);
        cCol.add(cLbl); cCol.add(Box.createVerticalStrut(4)); cCol.add(cityField);
        JPanel pCol = new JPanel(); pCol.setLayout(new BoxLayout(pCol, BoxLayout.Y_AXIS)); pCol.setOpaque(false);
        JLabel pLbl = new JLabel("PIN Code"); pLbl.setFont(Theme.FONT_SMALL_BOLD); pLbl.setForeground(Theme.DARK_TEXT);
        pCol.add(pLbl); pCol.add(Box.createVerticalStrut(4)); pCol.add(pinField);
        cityPinRow.add(cCol); cityPinRow.add(pCol);
        card.add(cityPinRow);

        card.add(Box.createVerticalStrut(20));

        JLabel h2 = new JLabel("2. Payment Preference (Demo)");
        h2.setFont(Theme.FONT_TITLE);
        h2.setForeground(Theme.DARK_TEXT);
        card.add(h2);

        card.add(Box.createVerticalStrut(8));

        paymentMethodCombo = new JComboBox<>(new String[]{
                "Cash on Delivery",
                "UPI (Google Pay / PhonePe / Paytm)",
                "Credit / Debit Card",
                "Net Banking"
        });
        paymentMethodCombo.setFont(Theme.FONT_BODY);
        paymentMethodCombo.setBackground(Color.WHITE);
        paymentMethodCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        card.add(paymentMethodCombo);

        card.add(Box.createVerticalStrut(14));

        JLabel note = new JLabel("<html><i>Note: All payment options are simulated for evaluation purposes. No real monetary transactions occur.</i></html>");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.MUTED_TEXT);
        card.add(note);

        return card;
    }

    private JPanel createOrderSummaryCard() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("Order Review");
        h1.setFont(Theme.FONT_TITLE);
        h1.setForeground(Theme.DARK_TEXT);
        card.add(h1);

        card.add(Box.createVerticalStrut(12));

        // Line Items Summary
        orderItemsSummaryContainer = new JPanel();
        orderItemsSummaryContainer.setLayout(new BoxLayout(orderItemsSummaryContainer, BoxLayout.Y_AXIS));
        orderItemsSummaryContainer.setOpaque(false);

        for (CartItem item : cartManager.getItems()) {
            JPanel line = new JPanel(new BorderLayout());
            line.setOpaque(false);
            line.setMaximumSize(new Dimension(Short.MAX_VALUE, 22));

            String name = item.getProduct().getName();
            if (name.length() > 28) name = name.substring(0, 25) + "...";

            JLabel left = new JLabel(name + " × " + item.getQuantity());
            left.setFont(Theme.FONT_SMALL);
            left.setForeground(Theme.DARK_TEXT);
            line.add(left, BorderLayout.WEST);

            JLabel right = new JLabel(Theme.formatRupee(item.getSubtotal()));
            right.setFont(Theme.FONT_SMALL_BOLD);
            right.setForeground(Theme.DARK_TEXT);
            line.add(right, BorderLayout.EAST);

            orderItemsSummaryContainer.add(line);
            orderItemsSummaryContainer.add(Box.createVerticalStrut(4));
        }

        card.add(orderItemsSummaryContainer);
        card.add(Box.createVerticalStrut(12));

        // Coupon Section
        JLabel cTitle = new JLabel("Promotional Voucher Code");
        cTitle.setFont(Theme.FONT_SMALL_BOLD);
        cTitle.setForeground(Theme.MUTED_TEXT);
        card.add(cTitle);

        card.add(Box.createVerticalStrut(4));

        JPanel couponRow = new JPanel(new BorderLayout(8, 0));
        couponRow.setOpaque(false);
        couponRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));

        couponField = Theme.createTextField(12);
        couponRow.add(couponField, BorderLayout.CENTER);

        JButton applyBtn = Theme.createSecondaryButton("Apply");
        applyBtn.addActionListener(e -> handleApplyCoupon());
        couponRow.add(applyBtn, BorderLayout.EAST);

        card.add(couponRow);

        couponFeedbackLabel = new JLabel("Try codes: WELCOME10, SAVE20, FLAT100");
        couponFeedbackLabel.setFont(Theme.FONT_SMALL);
        couponFeedbackLabel.setForeground(Theme.MUTED_TEXT);
        card.add(couponFeedbackLabel);

        card.add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDERS);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        // Calculations Breakdown
        subtotalLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Subtotal", subtotalLabel);

        discountLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Promotional Discount", discountLabel);

        gstLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Statutory GST (18%)", gstLabel);

        deliveryLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Delivery Fee", deliveryLabel);

        card.add(Box.createVerticalStrut(8));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(Theme.BORDERS);
        card.add(sep2);
        card.add(Box.createVerticalStrut(8));

        totalLabel = new JLabel("₹0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(Theme.DARK_TEXT);
        addSummaryRow(card, "Final Amount", totalLabel);

        card.add(Box.createVerticalStrut(22));

        JButton placeOrderBtn = Theme.createPrimaryButton("Confirm & Place Boutique Order");
        placeOrderBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        placeOrderBtn.setFont(Theme.FONT_BODY_BOLD);
        placeOrderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeOrderBtn.addActionListener(e -> handlePlaceOrder());
        card.add(placeOrderBtn);

        return card;
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SMALL_BOLD);
        label.setForeground(Theme.DARK_TEXT);
        parent.add(label);
        parent.add(Box.createVerticalStrut(4));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private void addSummaryRow(JPanel parent, String labelText, JLabel valLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));

        JLabel l = new JLabel(labelText);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.MUTED_TEXT);
        row.add(l, BorderLayout.WEST);

        valLabel.setFont(Theme.FONT_BODY_BOLD);
        valLabel.setForeground(Theme.DARK_TEXT);
        row.add(valLabel, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(4));
    }

    private void handleApplyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            couponFeedbackLabel.setText("Please enter a coupon code.");
            couponFeedbackLabel.setForeground(Theme.DANGER);
            return;
        }

        DiscountService.CouponResult res = discountService.applyCoupon(code, cartManager.getSubtotal());
        if (res.isSuccess()) {
            appliedCouponCode = res.getCode();
            couponFeedbackLabel.setText(res.getMessage());
            couponFeedbackLabel.setForeground(Theme.SUCCESS);
            recalculateSummary();
        } else {
            appliedCouponCode = null;
            couponFeedbackLabel.setText(res.getMessage());
            couponFeedbackLabel.setForeground(Theme.DANGER);
            recalculateSummary();
        }
    }

    private void recalculateSummary() {
        BillingService.BillingSummary summary = billingService.calculateBilling(cartManager.getCart(), appliedCouponCode);

        if (subtotalLabel != null) subtotalLabel.setText(Theme.formatRupee(summary.getSubtotal()));
        if (discountLabel != null) {
            discountLabel.setText(summary.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                    ? "- " + Theme.formatRupee(summary.getDiscountAmount())
                    : "₹0.00");
        }
        if (gstLabel != null) gstLabel.setText(Theme.formatRupee(summary.getGstAmount()));
        if (deliveryLabel != null) {
            deliveryLabel.setText(summary.getDeliveryFee().compareTo(BigDecimal.ZERO) == 0 ? "FREE" : Theme.formatRupee(summary.getDeliveryFee()));
        }
        if (totalLabel != null) totalLabel.setText(Theme.formatRupee(summary.getFinalPayableAmount()));
    }

    private void handlePlaceOrder() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String pin = pinField.getText().trim();
        String payment = (String) paymentMethodCombo.getSelectedItem();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all delivery details before placing your order.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer current = userManager.getCurrentUser();
        if (current == null) {
            JOptionPane.showMessageDialog(this,
                    "You must be authenticated to place an order.",
                    "Authentication Required", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullAddress = address + ", " + city + " - " + pin;
        BillingService.BillingSummary summary = billingService.calculateBilling(cartManager.getCart(), appliedCouponCode);

        try {
            Order order = orderManager.placeOrder(current, cartManager.getCart(), summary, fullAddress, payment);
            cartManager.clearCart();

            if (homeFrame != null) {
                homeFrame.updateCartBadge();
                homeFrame.showToast("Order Placed Successfully!");
            }

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Thank you for shopping with Maisonelle!\n\n" +
                    "Order ID: " + order.getOrderId() + "\n" +
                    "Final Total: " + Theme.formatRupee(order.getTotalAmount()) + "\n\n" +
                    "A formal invoice has been generated in your 'invoices/' folder.",
                    "Order Confirmed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"View Invoice (.txt)", "View Order History"},
                    "View Invoice (.txt)"
            );

            if (choice == 0 && order.getInvoiceFilePath() != null) {
                InvoiceGenerator.openInvoiceFile(new File(order.getInvoiceFilePath()));
            }

            if (homeFrame != null) {
                homeFrame.showOrderHistory();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Order placement failed: " + ex.getMessage(),
                    "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
