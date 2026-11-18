package com.maisonelle.ui;

import com.maisonelle.manager.CartManager;
import com.maisonelle.manager.WishlistManager;
import com.maisonelle.model.Product;
import com.maisonelle.util.ImageUtils;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dedicated Product Details View displaying high-resolution imagery,
 * stock status, quantity controller, and direct purchase actions.
 */
public class ProductDetailsPanel extends JPanel {

    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final HomeFrame homeFrame;

    private Product product;
    private int selectedQuantity = 1;

    private JLabel imageLabel;
    private JLabel categoryBadge;
    private JLabel brandLabel;
    private JLabel nameLabel;
    private JLabel ratingLabel;
    private JLabel stockBadge;
    private JLabel priceLabel;
    private JLabel origPriceLabel;
    private JLabel discountBadge;
    private JTextArea descArea;
    private JLabel qtyValueLabel;
    private JButton addToCartBtn;
    private JButton wishlistBtn;

    public ProductDetailsPanel(CartManager cartManager, WishlistManager wishlistManager, HomeFrame homeFrame) {
        this.cartManager = cartManager;
        this.wishlistManager = wishlistManager;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(18, 30, 14, 30));

        JButton backBtn = Theme.createSecondaryButton("← Back to Catalogue");
        backBtn.addActionListener(e -> {
            if (homeFrame != null) {
                homeFrame.showCatalogue();
            }
        });
        topBar.add(backBtn, BorderLayout.WEST);

        JLabel navTitle = new JLabel("Boutique Product Specification", SwingConstants.CENTER);
        navTitle.setFont(Theme.FONT_SUBTITLE);
        navTitle.setForeground(Theme.MUTED_TEXT);
        topBar.add(navTitle, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);

        // Center Split Content
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 32, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 32, 30, 32));

        // LEFT COLUMN: Product Image Presentation Card
        JPanel leftCard = Theme.createCardPanel();
        leftCard.setLayout(new BorderLayout());
        leftCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        imageLabel = new JLabel("", SwingConstants.CENTER);
        leftCard.add(imageLabel, BorderLayout.CENTER);
        centerPanel.add(leftCard);

        // RIGHT COLUMN: Specifications & Purchasing Controls
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        // Category & Brand
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaRow.setOpaque(false);
        categoryBadge = Theme.createBadge("CATEGORY", Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);
        metaRow.add(categoryBadge);

        brandLabel = new JLabel("BRAND");
        brandLabel.setFont(Theme.FONT_SMALL_BOLD);
        brandLabel.setForeground(Theme.MUTED_TEXT);
        metaRow.add(brandLabel);
        rightCol.add(metaRow);

        rightCol.add(Box.createVerticalStrut(10));

        nameLabel = new JLabel("Product Name");
        nameLabel.setFont(Theme.FONT_TITLE);
        nameLabel.setForeground(Theme.DARK_TEXT);
        rightCol.add(nameLabel);

        rightCol.add(Box.createVerticalStrut(8));

        // Rating and stock
        JPanel rateStockRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rateStockRow.setOpaque(false);

        ratingLabel = new JLabel("★ 4.8 (Curated Selection)");
        ratingLabel.setFont(Theme.FONT_BODY_BOLD);
        ratingLabel.setForeground(Theme.MAIN_HEADINGS);
        rateStockRow.add(ratingLabel);

        stockBadge = Theme.createBadge("In Stock", new Color(94, 126, 99, 30), Theme.SUCCESS);
        rateStockRow.add(stockBadge);
        rightCol.add(rateStockRow);

        rightCol.add(Box.createVerticalStrut(16));

        // Price Block
        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        priceRow.setOpaque(false);

        priceLabel = new JLabel("₹0.00");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        priceLabel.setForeground(Theme.DARK_TEXT);
        priceRow.add(priceLabel);

        origPriceLabel = new JLabel("");
        origPriceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        origPriceLabel.setForeground(Theme.MUTED_TEXT);
        priceRow.add(origPriceLabel);

        discountBadge = Theme.createBadge("0% OFF", Theme.SOFT_ACCENT, Theme.DARK_TEXT);
        priceRow.add(discountBadge);
        rightCol.add(priceRow);

        rightCol.add(Box.createVerticalStrut(16));

        // Description
        JLabel descTitle = new JLabel("About this piece:");
        descTitle.setFont(Theme.FONT_SMALL_BOLD);
        descTitle.setForeground(Theme.MUTED_TEXT);
        rightCol.add(descTitle);

        rightCol.add(Box.createVerticalStrut(4));

        descArea = new JTextArea(4, 25);
        descArea.setFont(Theme.FONT_BODY);
        descArea.setForeground(Theme.DARK_TEXT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(null);
        rightCol.add(descArea);

        rightCol.add(Box.createVerticalStrut(20));

        // Quantity Selector
        JPanel qtyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qtyRow.setOpaque(false);

        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(Theme.FONT_BODY_BOLD);
        qtyLbl.setForeground(Theme.DARK_TEXT);
        qtyRow.add(qtyLbl);

        JButton minusBtn = Theme.createSecondaryButton("−");
        minusBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        minusBtn.addActionListener(e -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                qtyValueLabel.setText(String.valueOf(selectedQuantity));
            }
        });
        qtyRow.add(minusBtn);

        qtyValueLabel = new JLabel("1", SwingConstants.CENTER);
        qtyValueLabel.setFont(Theme.FONT_HEADER);
        qtyValueLabel.setForeground(Theme.DARK_TEXT);
        qtyValueLabel.setPreferredSize(new Dimension(32, 28));
        qtyRow.add(qtyValueLabel);

        JButton plusBtn = Theme.createSecondaryButton("+");
        plusBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        plusBtn.addActionListener(e -> {
            if (product != null && selectedQuantity < product.getStock()) {
                selectedQuantity++;
                qtyValueLabel.setText(String.valueOf(selectedQuantity));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Maximum available boutique stock reached for this piece.",
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
            }
        });
        qtyRow.add(plusBtn);

        rightCol.add(qtyRow);

        rightCol.add(Box.createVerticalStrut(22));

        // Action Buttons Row (Add to cart & Wishlist)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setOpaque(false);

        addToCartBtn = Theme.createPrimaryButton("🛍️  Add to Cart");
        addToCartBtn.setPreferredSize(new Dimension(180, 42));
        addToCartBtn.addActionListener(e -> handleAddToCart());
        actionRow.add(addToCartBtn);

        wishlistBtn = Theme.createOutlineButton("🤍  Save to Wishlist");
        wishlistBtn.setPreferredSize(new Dimension(180, 42));
        wishlistBtn.addActionListener(e -> handleToggleWishlist());
        actionRow.add(wishlistBtn);

        rightCol.add(actionRow);

        centerPanel.add(rightCol);

        JScrollPane sp = new JScrollPane(centerPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        add(sp, BorderLayout.CENTER);
    }

    public void setProduct(Product p) {
        this.product = p;
        this.selectedQuantity = 1;
        if (qtyValueLabel != null) {
            qtyValueLabel.setText("1");
        }

        if (p == null) return;

        imageLabel.setIcon(ImageUtils.loadProductImage(p.getImagePath(), 340, 340));
        categoryBadge.setText(p.getCategory().toUpperCase());
        brandLabel.setText(p.getBrand().toUpperCase());
        nameLabel.setText(p.getName());
        ratingLabel.setText("★ " + String.format("%.1f", p.getRating()) + " (Curated Selection)");

        priceLabel.setText(Theme.formatRupee(p.getPrice()));

        if (p.hasDiscount()) {
            origPriceLabel.setText("<html><strike>" + Theme.formatRupee(p.getOriginalPrice()) + "</strike></html>");
            origPriceLabel.setVisible(true);
            discountBadge.setText(p.getDiscountPercentage() + "% OFF");
            discountBadge.setVisible(true);
        } else {
            origPriceLabel.setVisible(false);
            discountBadge.setVisible(false);
        }

        if (p.getStock() <= 0) {
            stockBadge.setText("Out of Stock");
            stockBadge.setForeground(Theme.DANGER);
            addToCartBtn.setEnabled(false);
            addToCartBtn.setText("Sold Out");
        } else if (p.getStock() <= 8) {
            stockBadge.setText("Only " + p.getStock() + " left in stock");
            stockBadge.setForeground(Theme.MAIN_HEADINGS);
            addToCartBtn.setEnabled(true);
            addToCartBtn.setText("🛍️  Add to Cart");
        } else {
            stockBadge.setText("In Stock (" + p.getStock() + " available)");
            stockBadge.setForeground(Theme.SUCCESS);
            addToCartBtn.setEnabled(true);
            addToCartBtn.setText("🛍️  Add to Cart");
        }

        descArea.setText(p.getDescription());

        boolean inWish = wishlistManager != null && wishlistManager.isInWishlist(p.getId());
        wishlistBtn.setText(inWish ? "❤️  In Saved Wishlist" : "🤍  Save to Wishlist");
    }

    private void handleAddToCart() {
        if (product == null || product.getStock() <= 0) return;

        boolean added = cartManager.addToCart(product, selectedQuantity);
        if (added) {
            if (homeFrame != null) {
                homeFrame.updateCartBadge();
                homeFrame.showToast("Added " + selectedQuantity + "x " + product.getName() + " to Cart");
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cannot add " + selectedQuantity + " item(s). Available stock limit exceeded.",
                    "Stock Limit Exceeded", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleToggleWishlist() {
        if (product == null || wishlistManager == null) return;

        boolean nowInWish = wishlistManager.toggleWishlist(product);
        wishlistBtn.setText(nowInWish ? "❤️  In Saved Wishlist" : "🤍  Save to Wishlist");
        if (homeFrame != null) {
            homeFrame.updateWishlistBadge();
            homeFrame.showToast(nowInWish ? "Saved to Wishlist: " + product.getName() : "Removed from Wishlist");
        }
    }
}
