package com.maisonelle.ui;

import com.maisonelle.manager.CartManager;
import com.maisonelle.manager.WishlistManager;
import com.maisonelle.model.Product;
import com.maisonelle.util.ImageUtils;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Saved Wishlist Panel displaying curated customer bookmarks,
 * with one-click transfer to the active shopping cart.
 */
public class WishlistPanel extends JPanel {

    private final WishlistManager wishlistManager;
    private final CartManager cartManager;
    private final HomeFrame homeFrame;

    private JPanel gridContainer;
    private JLabel countBadgeLabel;

    public WishlistPanel(WishlistManager wishlistManager, CartManager cartManager, HomeFrame homeFrame) {
        this.wishlistManager = wishlistManager;
        this.cartManager = cartManager;
        this.homeFrame = homeFrame;

        setLayout(new BorderLayout());
        setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        removeAll();

        // Header Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 32, 14, 32));

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("Your Saved Wishlist");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.DARK_TEXT);
        titleGroup.add(title);

        countBadgeLabel = Theme.createBadge("0 Saved", Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);
        titleGroup.add(countBadgeLabel);
        topBar.add(titleGroup, BorderLayout.WEST);

        JButton backBtn = Theme.createSecondaryButton("← Continue Shopping");
        backBtn.addActionListener(e -> {
            if (homeFrame != null) homeFrame.showCatalogue();
        });
        topBar.add(backBtn, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Grid Container
        gridContainer = new JPanel();
        gridContainer.setOpaque(false);
        gridContainer.setBorder(new EmptyBorder(10, 32, 30, 32));

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        refreshWishlist();
    }

    public void refreshWishlist() {
        if (gridContainer == null) return;
        gridContainer.removeAll();

        List<Product> items = wishlistManager.getWishlistProducts();
        if (countBadgeLabel != null) {
            countBadgeLabel.setText(items.size() + (items.size() == 1 ? " Piece" : " Pieces"));
        }

        if (items.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(80, 0, 80, 0));

            JLabel icon = new JLabel("🤍");
            icon.setFont(new Font("SansSerif", Font.PLAIN, 46));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(icon);

            empty.add(Box.createVerticalStrut(12));

            JLabel title = new JLabel("Your wishlist is currently empty.");
            title.setFont(Theme.FONT_TITLE);
            title.setForeground(Theme.DARK_TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(title);

            empty.add(Box.createVerticalStrut(6));

            JLabel sub = new JLabel("Save pieces you love while exploring our boutique catalogue.");
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

            gridContainer.setLayout(new BorderLayout());
            gridContainer.add(empty, BorderLayout.CENTER);
        } else {
            gridContainer.setLayout(new GridLayout(0, 3, 20, 20));
            for (Product p : items) {
                gridContainer.add(createWishlistCard(p));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createWishlistCard(Product p) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Theme.BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(240, 360));

        // Top Thumbnail
        ImageIcon icon = ImageUtils.loadProductImage(p.getImagePath(), 210, 160);
        JLabel img = new JLabel(icon, SwingConstants.CENTER);
        img.setBackground(Color.WHITE);
        img.setOpaque(true);
        img.setCursor(new Cursor(Cursor.HAND_CURSOR));
        img.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (homeFrame != null) homeFrame.showProductDetails(p);
            }
        });
        card.add(img, BorderLayout.NORTH);

        // Center Details
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel brand = new JLabel(p.getBrand().toUpperCase());
        brand.setFont(Theme.FONT_SMALL_BOLD);
        brand.setForeground(Theme.MUTED_TEXT);
        center.add(brand);

        JLabel name = new JLabel(p.getName());
        name.setFont(Theme.FONT_HEADER);
        name.setForeground(Theme.DARK_TEXT);
        name.setCursor(new Cursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (homeFrame != null) homeFrame.showProductDetails(p);
            }
        });
        center.add(name);

        center.add(Box.createVerticalStrut(4));

        JLabel price = new JLabel(Theme.formatRupee(p.getPrice()));
        price.setFont(Theme.FONT_HEADER);
        price.setForeground(Theme.DARK_TEXT);
        center.add(price);

        card.add(center, BorderLayout.CENTER);

        // Bottom Actions (Move to Cart & Remove)
        JPanel bot = new JPanel(new GridLayout(1, 2, 8, 0));
        bot.setOpaque(false);

        JButton moveBtn = Theme.createPrimaryButton("🛍️ Move to Bag");
        moveBtn.setFont(Theme.FONT_SMALL_BOLD);
        moveBtn.setEnabled(p.getStock() > 0);
        moveBtn.addActionListener(e -> {
            boolean transferred = wishlistManager.moveToCart(p.getId(), cartManager);
            if (transferred) {
                refreshWishlist();
                if (homeFrame != null) {
                    homeFrame.updateCartBadge();
                    homeFrame.updateWishlistBadge();
                    homeFrame.showToast("Moved " + p.getName() + " to Shopping Bag");
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not transfer item. It may be out of stock.",
                        "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
        bot.add(moveBtn);

        JButton removeBtn = Theme.createSecondaryButton("Remove");
        removeBtn.setFont(Theme.FONT_SMALL_BOLD);
        removeBtn.addActionListener(e -> {
            wishlistManager.removeFromWishlist(p.getId());
            refreshWishlist();
            if (homeFrame != null) {
                homeFrame.updateWishlistBadge();
                homeFrame.showToast("Removed from Wishlist");
            }
        });
        bot.add(removeBtn);

        card.add(bot, BorderLayout.SOUTH);

        return card;
    }
}
