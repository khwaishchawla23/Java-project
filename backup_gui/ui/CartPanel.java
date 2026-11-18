package com.maisonelle.ui;

import com.maisonelle.manager.CartManager;
import com.maisonelle.model.CartItem;
import com.maisonelle.model.Product;
import com.maisonelle.service.BillingService;
import com.maisonelle.util.ImageUtils;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;

/**
 * Interactive Shopping Bag Panel with live quantity controllers,
 * line item removal, subtotal recalculations, and checkout handoff.
 */
public class CartPanel extends JPanel {

    private final CartManager cartManager;
    private final BillingService billingService;
    private final HomeFrame homeFrame;

    private JPanel itemsContainer;
    private JLabel summarySubtotalLabel;
    private JLabel summaryGstLabel;
    private JLabel summaryDeliveryLabel;
    private JLabel summaryTotalLabel;
    private JLabel itemCountHeaderLabel;

    public CartPanel(CartManager cartManager, BillingService billingService, HomeFrame homeFrame) {
        this.cartManager = cartManager;
        this.billingService = billingService;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        removeAll();

        // Top Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 32, 14, 32));

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("Your Boutique Shopping Bag");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        titleGroup.add(title);

        itemCountHeaderLabel = Theme.createBadge("0 Items", Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);
        titleGroup.add(itemCountHeaderLabel);
        topBar.add(titleGroup, BorderLayout.WEST);

        JButton continueBtn = Theme.createSecondaryButton("← Continue Shopping");
        continueBtn.addActionListener(e -> {
            if (homeFrame != null) homeFrame.showCatalogue();
        });
        topBar.add(continueBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Split View
        JPanel centerPanel = new JPanel(new BorderLayout(24, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(6, 32, 28, 32));

        // LEFT: Cart Items Scroll View
        itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // RIGHT: Order Summary Card
        centerPanel.add(createSummaryPanel(), BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        refreshCart();
    }

    private JPanel createSummaryPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(320, 400));

        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order Summary");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        card.add(title);

        card.add(Box.createVerticalStrut(16));

        summarySubtotalLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Subtotal", summarySubtotalLabel);

        summaryGstLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Estimated GST (18%)", summaryGstLabel);

        summaryDeliveryLabel = new JLabel("₹0.00");
        addSummaryRow(card, "Standard Delivery", summaryDeliveryLabel);

        card.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDERS);
        card.add(sep);
        card.add(Box.createVerticalStrut(10));

        summaryTotalLabel = new JLabel("₹0.00");
        summaryTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        summaryTotalLabel.setForeground(Theme.DARK_TEXT);
        addSummaryRow(card, "Estimated Total", summaryTotalLabel);

        card.add(Box.createVerticalStrut(20));

        JButton checkoutBtn = Theme.createPrimaryButton("Proceed to Checkout →");
        checkoutBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        checkoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutBtn.addActionListener(e -> {
            if (cartManager.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Your shopping bag is empty. Please add items before proceeding.",
                        "Empty Bag", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (homeFrame != null) {
                homeFrame.showCheckout();
            }
        });
        card.add(checkoutBtn);

        card.add(Box.createVerticalStrut(12));

        JButton clearBtn = new JButton("Clear Entire Bag");
        clearBtn.setFont(Theme.FONT_SMALL_BOLD);
        clearBtn.setForeground(Theme.DANGER);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> {
            if (!cartManager.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you wish to clear your shopping bag?",
                        "Confirm Clear", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    cartManager.clearCart();
                    refreshCart();
                    if (homeFrame != null) {
                        homeFrame.updateCartBadge();
                        homeFrame.showToast("Shopping bag cleared");
                    }
                }
            }
        });
        card.add(clearBtn);

        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
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
        parent.add(Box.createVerticalStrut(6));
    }

    public void refreshCart() {
        if (itemsContainer == null) return;
        itemsContainer.removeAll();

        int count = cartManager.getTotalItemCount();
        if (itemCountHeaderLabel != null) {
            itemCountHeaderLabel.setText(count + (count == 1 ? " Piece" : " Pieces"));
        }

        if (cartManager.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(80, 0, 80, 0));

            JLabel icon = new JLabel("🛍️");
            icon.setFont(new Font("SansSerif", Font.PLAIN, 46));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(icon);

            empty.add(Box.createVerticalStrut(12));

            JLabel title = new JLabel("Your shopping bag is currently empty.");
            title.setFont(Theme.FONT_TITLE);
            title.setForeground(Theme.DARK_TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(title);

            empty.add(Box.createVerticalStrut(6));

            JLabel sub = new JLabel("Explore our thoughtfully curated collection to discover timeless pieces.");
            sub.setFont(Theme.FONT_BODY);
            sub.setForeground(Theme.MUTED_TEXT);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(sub);

            empty.add(Box.createVerticalStrut(18));

            JButton shopBtn = Theme.createPrimaryButton("Browse Catalogue");
            shopBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            shopBtn.addActionListener(e -> {
                if (homeFrame != null) homeFrame.showCatalogue();
            });
            empty.add(shopBtn);

            itemsContainer.add(empty);

            updateSummaryAmounts(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        } else {
            for (CartItem item : cartManager.getItems()) {
                itemsContainer.add(createCartItemCard(item));
                itemsContainer.add(Box.createVerticalStrut(12));
            }

            BillingService.BillingSummary summary = billingService.calculateBilling(cartManager.getCart(), null);
            updateSummaryAmounts(summary.getSubtotal(), summary.getGstAmount(), summary.getDeliveryFee(), summary.getFinalPayableAmount());
        }

        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    private void updateSummaryAmounts(BigDecimal subtotal, BigDecimal gst, BigDecimal delivery, BigDecimal total) {
        if (summarySubtotalLabel != null) summarySubtotalLabel.setText(Theme.formatRupee(subtotal));
        if (summaryGstLabel != null) summaryGstLabel.setText(Theme.formatRupee(gst));
        if (summaryDeliveryLabel != null) {
            summaryDeliveryLabel.setText(delivery.compareTo(BigDecimal.ZERO) == 0 ? "FREE" : Theme.formatRupee(delivery));
        }
        if (summaryTotalLabel != null) summaryTotalLabel.setText(Theme.formatRupee(total));
    }

    private JPanel createCartItemCard(CartItem item) {
        Product p = item.getProduct();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

        // Left Thumbnail
        ImageIcon icon = ImageUtils.loadProductImage(p.getImagePath(), 70, 70);
        JLabel img = new JLabel(icon);
        img.setBackground(Color.WHITE);
        img.setOpaque(true);
        img.setPreferredSize(new Dimension(70, 70));
        card.add(img, BorderLayout.WEST);

        // Center Product Details
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel brand = new JLabel(p.getBrand().toUpperCase() + " • " + p.getCategory().toUpperCase());
        brand.setFont(Theme.FONT_SMALL_BOLD);
        brand.setForeground(Theme.MUTED_TEXT);
        center.add(brand);

        JLabel name = new JLabel(p.getName());
        name.setFont(Theme.FONT_HEADER);
        name.setForeground(Theme.DARK_TEXT);
        center.add(name);

        JLabel price = new JLabel(Theme.formatRupee(p.getPrice()) + " each");
        price.setFont(Theme.FONT_SMALL);
        price.setForeground(Theme.MUTED_TEXT);
        center.add(price);

        card.add(center, BorderLayout.CENTER);

        // Right Quantity & Subtotal Controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        right.setOpaque(false);

        JButton minus = Theme.createSecondaryButton("−");
        minus.setFont(new Font("SansSerif", Font.BOLD, 13));
        minus.setPreferredSize(new Dimension(32, 28));
        minus.addActionListener(e -> {
            cartManager.updateQuantity(p.getId(), item.getQuantity() - 1);
            refreshCart();
            if (homeFrame != null) homeFrame.updateCartBadge();
        });
        right.add(minus);

        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()), SwingConstants.CENTER);
        qtyLabel.setFont(Theme.FONT_BODY_BOLD);
        qtyLabel.setForeground(Theme.DARK_TEXT);
        qtyLabel.setPreferredSize(new Dimension(24, 28));
        right.add(qtyLabel);

        JButton plus = Theme.createSecondaryButton("+");
        plus.setFont(new Font("SansSerif", Font.BOLD, 13));
        plus.setPreferredSize(new Dimension(32, 28));
        plus.addActionListener(e -> {
            if (item.getQuantity() < p.getStock()) {
                cartManager.updateQuantity(p.getId(), item.getQuantity() + 1);
                refreshCart();
                if (homeFrame != null) homeFrame.updateCartBadge();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Maximum available stock reached for this piece.",
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
            }
        });
        right.add(plus);

        JLabel subtotal = new JLabel(Theme.formatRupee(item.getSubtotal()));
        subtotal.setFont(Theme.FONT_HEADER);
        subtotal.setForeground(Theme.DARK_TEXT);
        subtotal.setPreferredSize(new Dimension(95, 28));
        right.add(subtotal);

        JButton delBtn = new JButton("✕");
        delBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        delBtn.setForeground(Theme.DANGER);
        delBtn.setContentAreaFilled(false);
        delBtn.setBorderPainted(false);
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            cartManager.removeFromCart(p.getId());
            refreshCart();
            if (homeFrame != null) {
                homeFrame.updateCartBadge();
                homeFrame.showToast("Removed " + p.getName() + " from Bag");
            }
        });
        right.add(delBtn);

        card.add(right, BorderLayout.EAST);

        return card;
    }
}
