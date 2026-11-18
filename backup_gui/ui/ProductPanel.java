package com.maisonelle.ui;

import com.maisonelle.manager.CartManager;
import com.maisonelle.manager.ProductManager;
import com.maisonelle.manager.WishlistManager;
import com.maisonelle.model.Product;
import com.maisonelle.util.ImageUtils;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main Boutique Product Catalogue browsing interface.
 * Implements category pills, search bar, sorting criteria,
 * section filters, and responsive product cards.
 */
public class ProductPanel extends JPanel {

    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final HomeFrame homeFrame;

    private String selectedCategory = "All";
    private String selectedSection = "All Products";

    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JLabel resultsCountLabel;
    private JPanel gridContainer;
    private JPanel categoryPillsPanel;
    private JPanel sectionTabsPanel;

    public ProductPanel(ProductManager productManager, CartManager cartManager,
                        WishlistManager wishlistManager, HomeFrame homeFrame) {
        this.productManager = productManager;
        this.cartManager = cartManager;
        this.wishlistManager = wishlistManager;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. Subtle Hero Header
        topContainer.add(createHeroHeader());

        // 2. Filter & Search Controls Bar
        topContainer.add(createControlsBar());

        add(topContainer, BorderLayout.NORTH);

        // 3. Product Grid Scroll Area
        add(createGridScrollPane(), BorderLayout.CENTER);

        refreshProducts();
    }

    private JPanel createHeroHeader() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(16, 28, 10, 28));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel h1 = new JLabel("Curated Boutique Collection");
        h1.setFont(Theme.FONT_TITLE);
        h1.setForeground(Theme.DARK_TEXT);
        leftText.add(h1);

        JLabel h2 = new JLabel("Find beauty in the everyday. Thoughtfully selected, beautifully priced.");
        h2.setFont(Theme.FONT_BODY);
        h2.setForeground(Theme.MUTED_TEXT);
        leftText.add(h2);

        hero.add(leftText, BorderLayout.WEST);

        // Quick Promo Pill (Right)
        JPanel promoPill = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        promoPill.setOpaque(false);
        promoPill.add(Theme.createBadge("PROMO: WELCOME10 (10% Off)", Theme.SECONDARY_BACKGROUND, Theme.MAIN_HEADINGS));
        promoPill.add(Theme.createBadge("SAVE20 (20% Off)", Theme.SOFT_ACCENT, Theme.DARK_TEXT));
        hero.add(promoPill, BorderLayout.EAST);

        return hero;
    }

    private JPanel createControlsBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 8));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 28, 12, 28));

        // Section Tabs Row (All Products, Featured, Best Sellers, Special Offers)
        sectionTabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sectionTabsPanel.setOpaque(false);
        rebuildSectionTabs();
        bar.add(sectionTabsPanel, BorderLayout.NORTH);

        // Middle Search & Sort Row
        JPanel middleRow = new JPanel(new BorderLayout(16, 0));
        middleRow.setOpaque(false);

        resultsCountLabel = new JLabel("Showing all products");
        resultsCountLabel.setFont(Theme.FONT_SMALL);
        resultsCountLabel.setForeground(Theme.MUTED_TEXT);
        middleRow.add(resultsCountLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        searchField = Theme.createTextField(18);
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshProducts();
            }
        });
        rightControls.add(new JLabel("🔍"));
        rightControls.add(searchField);

        sortCombo = new JComboBox<>(new String[]{
                "Featured & Curated",
                "Price: Low to High",
                "Price: High to Low",
                "Biggest Discounts (% Off)",
                "Top Customer Rating",
                "Name: A to Z"
        });
        sortCombo.setFont(Theme.FONT_BODY);
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setPreferredSize(new Dimension(190, 36));
        sortCombo.addActionListener(e -> refreshProducts());
        rightControls.add(sortCombo);

        middleRow.add(rightControls, BorderLayout.EAST);
        bar.add(middleRow, BorderLayout.CENTER);

        // Category Filter Pills
        categoryPillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        categoryPillsPanel.setOpaque(false);
        rebuildCategoryPills();
        bar.add(categoryPillsPanel, BorderLayout.SOUTH);

        return bar;
    }

    private void rebuildSectionTabs() {
        if (sectionTabsPanel == null) return;
        sectionTabsPanel.removeAll();

        String[] sections = {"All Products", "🌟 Featured Pieces", "🔥 Best Sellers", "🏷️ Special Offers"};
        for (String sec : sections) {
            JButton btn = new JButton(sec) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean isSelected = sec.equalsIgnoreCase(selectedSection);
                    if (isSelected) {
                        g2.setColor(Theme.PRIMARY_BUTTON);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    } else if (getModel().isRollover()) {
                        g2.setColor(Theme.SECONDARY_BACKGROUND);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boolean isSel = sec.equalsIgnoreCase(selectedSection);
            btn.setFont(Theme.FONT_SMALL_BOLD);
            btn.setForeground(isSel ? Color.WHITE : Theme.DARK_TEXT);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new EmptyBorder(6, 12, 6, 12));
            btn.addActionListener(e -> {
                selectedSection = sec;
                rebuildSectionTabs();
                refreshProducts();
            });
            sectionTabsPanel.add(btn);
        }
        sectionTabsPanel.revalidate();
        sectionTabsPanel.repaint();
    }

    private void rebuildCategoryPills() {
        if (categoryPillsPanel == null) return;
        categoryPillsPanel.removeAll();

        for (String cat : productManager.getCategories()) {
            JButton pill = new JButton(cat) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean isSelected = cat.equalsIgnoreCase(selectedCategory);
                    if (isSelected) {
                        g2.setColor(Theme.DARK_TEXT);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    } else if (getModel().isRollover()) {
                        g2.setColor(Theme.SOFT_ACCENT);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                        g2.setColor(Theme.BORDERS);
                        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            boolean isSel = cat.equalsIgnoreCase(selectedCategory);
            pill.setFont(Theme.FONT_SMALL_BOLD);
            pill.setForeground(isSel ? Color.WHITE : Theme.DARK_TEXT);
            pill.setContentAreaFilled(false);
            pill.setBorderPainted(false);
            pill.setFocusPainted(false);
            pill.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pill.setBorder(new EmptyBorder(6, 14, 6, 14));
            pill.addActionListener(e -> {
                selectedCategory = cat;
                rebuildCategoryPills();
                refreshProducts();
            });
            categoryPillsPanel.add(pill);
        }
        categoryPillsPanel.revalidate();
        categoryPillsPanel.repaint();
    }

    private JScrollPane createGridScrollPane() {
        gridContainer = new JPanel(new GridLayout(0, 3, 20, 20));
        gridContainer.setOpaque(false);
        gridContainer.setBorder(new EmptyBorder(10, 28, 30, 28));

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    public void refreshProducts() {
        if (gridContainer == null) return;
        gridContainer.removeAll();

        String query = searchField != null ? searchField.getText().trim() : "";
        String sort = sortCombo != null ? (String) sortCombo.getSelectedItem() : "Featured & Curated";

        List<Product> list = productManager.filterAndSort(selectedCategory, query, sort);

        if ("🌟 Featured Pieces".equalsIgnoreCase(selectedSection)) {
            list = list.stream().filter(Product::isFeatured).collect(Collectors.toList());
        } else if ("🔥 Best Sellers".equalsIgnoreCase(selectedSection)) {
            list = list.stream().filter(p -> p.getRating() >= 4.7).collect(Collectors.toList());
        } else if ("🏷️ Special Offers".equalsIgnoreCase(selectedSection)) {
            list = list.stream().filter(Product::hasDiscount).collect(Collectors.toList());
        }

        if (resultsCountLabel != null) {
            resultsCountLabel.setText("Showing " + list.size() + " pieces in " + selectedCategory + " (" + selectedSection + ")");
        }

        if (list.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(60, 0, 60, 0));

            JLabel icon = new JLabel("🔍");
            icon.setFont(new Font("SansSerif", Font.PLAIN, 42));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(icon);

            empty.add(Box.createVerticalStrut(12));

            JLabel msg = new JLabel("No boutique pieces found matching your criteria.");
            msg.setFont(Theme.FONT_SUBTITLE);
            msg.setForeground(Theme.DARK_TEXT);
            msg.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(msg);

            empty.add(Box.createVerticalStrut(14));

            JButton resetBtn = Theme.createPrimaryButton("Reset Filters");
            resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            resetBtn.addActionListener(e -> {
                if (searchField != null) searchField.setText("");
                selectedCategory = "All";
                selectedSection = "All Products";
                rebuildSectionTabs();
                rebuildCategoryPills();
                refreshProducts();
            });
            empty.add(resetBtn);

            gridContainer.setLayout(new BorderLayout());
            gridContainer.add(empty, BorderLayout.CENTER);
        } else {
            gridContainer.setLayout(new GridLayout(0, 3, 20, 20));
            for (Product p : list) {
                gridContainer.add(createProductCard(p));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createProductCard(Product product) {
        final boolean[] hovered = {false};

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

                if (hovered[0]) {
                    g2.setColor(Theme.PRIMARY_BUTTON);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14));
                } else {
                    g2.setColor(Theme.BORDERS);
                    g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 14, 14));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(260, 420));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered[0] = true;
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered[0] = false;
                card.repaint();
            }
        });

        // ----------------- TOP: Badge Row & Wishlist -----------------
        JPanel topBadgeBar = new JPanel(new BorderLayout());
        topBadgeBar.setOpaque(false);

        JLabel catBadge = Theme.createBadge(product.getCategory().toUpperCase(), Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);
        topBadgeBar.add(catBadge, BorderLayout.WEST);

        JPanel rightBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightBadges.setOpaque(false);

        if (product.hasDiscount()) {
            JLabel discBadge = Theme.createBadge(product.getDiscountPercentage() + "% OFF", Theme.SOFT_ACCENT, Theme.DARK_TEXT);
            rightBadges.add(discBadge);
        }

        boolean inWish = wishlistManager != null && wishlistManager.isInWishlist(product.getId());
        JButton wishBtn = new JButton(inWish ? "❤️" : "🤍");
        wishBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        wishBtn.setContentAreaFilled(false);
        wishBtn.setBorderPainted(false);
        wishBtn.setFocusPainted(false);
        wishBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        wishBtn.setBorder(new EmptyBorder(0, 2, 0, 2));
        wishBtn.addActionListener(e -> {
            if (wishlistManager != null) {
                boolean nowSaved = wishlistManager.toggleWishlist(product);
                wishBtn.setText(nowSaved ? "❤️" : "🤍");
                if (homeFrame != null) {
                    homeFrame.updateWishlistBadge();
                    homeFrame.showToast(nowSaved ? "Saved to Wishlist: " + product.getName() : "Removed from Wishlist");
                }
            }
        });
        rightBadges.add(wishBtn);

        topBadgeBar.add(rightBadges, BorderLayout.EAST);

        // ----------------- CENTER: Product Photograph -----------------
        ImageIcon icon = ImageUtils.loadProductImage(product.getImagePath(), 230, 180);
        JLabel imgLabel = new JLabel(icon, SwingConstants.CENTER);
        imgLabel.setBackground(Color.WHITE);
        imgLabel.setOpaque(true);
        imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (homeFrame != null) {
                    homeFrame.showProductDetails(product);
                }
            }
        });

        JPanel topGroup = new JPanel(new BorderLayout(0, 6));
        topGroup.setOpaque(false);
        topGroup.add(topBadgeBar, BorderLayout.NORTH);
        topGroup.add(imgLabel, BorderLayout.CENTER);
        card.add(topGroup, BorderLayout.NORTH);

        // ----------------- BOTTOM: Details & Add to Cart -----------------
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.setOpaque(false);

        JLabel brandLbl = new JLabel(product.getBrand().toUpperCase());
        brandLbl.setFont(Theme.FONT_SMALL_BOLD);
        brandLbl.setForeground(Theme.MUTED_TEXT);
        bot.add(brandLbl);

        JLabel nameLbl = new JLabel(product.getName());
        nameLbl.setFont(Theme.FONT_HEADER);
        nameLbl.setForeground(Theme.DARK_TEXT);
        nameLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (homeFrame != null) {
                    homeFrame.showProductDetails(product);
                }
            }
        });
        bot.add(nameLbl);

        bot.add(Box.createVerticalStrut(4));

        // Price Row
        JPanel pRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pRow.setOpaque(false);

        JLabel priceLbl = new JLabel(Theme.formatRupee(product.getPrice()));
        priceLbl.setFont(Theme.FONT_HEADER);
        priceLbl.setForeground(Theme.DARK_TEXT);
        pRow.add(priceLbl);

        if (product.hasDiscount()) {
            JLabel origLbl = new JLabel("<html><strike>" + Theme.formatRupee(product.getOriginalPrice()) + "</strike></html>");
            origLbl.setFont(Theme.FONT_SMALL);
            origLbl.setForeground(Theme.MUTED_TEXT);
            pRow.add(origLbl);
        }

        bot.add(pRow);

        bot.add(Box.createVerticalStrut(6));

        // Rating & Stock
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaRow.setOpaque(false);

        JLabel ratingLbl = new JLabel("★ " + product.getRating());
        ratingLbl.setFont(Theme.FONT_SMALL_BOLD);
        ratingLbl.setForeground(Theme.MAIN_HEADINGS);
        metaRow.add(ratingLbl);

        if (product.getStock() <= 0) {
            metaRow.add(Theme.createBadge("Sold Out", new Color(181, 90, 82, 30), Theme.DANGER));
        } else if (product.getStock() <= 6) {
            metaRow.add(Theme.createBadge("Only " + product.getStock() + " left", Theme.SECONDARY_BACKGROUND, Theme.MAIN_HEADINGS));
        }
        bot.add(metaRow);

        bot.add(Box.createVerticalStrut(10));

        // Action Buttons Row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);

        JButton viewBtn = Theme.createSecondaryButton("Details");
        viewBtn.setFont(Theme.FONT_SMALL_BOLD);
        viewBtn.addActionListener(e -> {
            if (homeFrame != null) {
                homeFrame.showProductDetails(product);
            }
        });
        btnRow.add(viewBtn);

        JButton addCartBtn = Theme.createPrimaryButton(product.getStock() <= 0 ? "Out of Stock" : "Add to Cart");
        addCartBtn.setFont(Theme.FONT_SMALL_BOLD);
        addCartBtn.setEnabled(product.getStock() > 0);
        addCartBtn.addActionListener(e -> {
            if (cartManager.addToCart(product, 1)) {
                if (homeFrame != null) {
                    homeFrame.updateCartBadge();
                    homeFrame.showToast("Added " + product.getName() + " to Cart");
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot add piece. Stock limit exceeded.",
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnRow.add(addCartBtn);

        bot.add(btnRow);

        card.add(bot, BorderLayout.CENTER);

        return card;
    }
}
