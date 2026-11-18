package com.maisonelle.ui;

import com.maisonelle.manager.*;
import com.maisonelle.model.Customer;
import com.maisonelle.model.Product;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Application Frame for Maisonelle using Swing CardLayout.
 * Provides unified navigation, persistent sidebar, dynamic top bar,
 * live cart/wishlist counters, and smooth screen transitions.
 */
public class HomeFrame extends JFrame {

    public static final String CARD_CATALOGUE = "CATALOGUE";
    public static final String CARD_DETAILS   = "DETAILS";
    public static final String CARD_CART      = "CART";
    public static final String CARD_WISHLIST  = "WISHLIST";
    public static final String CARD_CHECKOUT  = "CHECKOUT";
    public static final String CARD_ORDERS    = "ORDERS";
    public static final String CARD_ADMIN     = "ADMIN";

    private final DataManager dataManager;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final OrderManager orderManager;
    private final DiscountService discountService;
    private final BillingService billingService;
    private final InvoiceGenerator invoiceGenerator;

    private CardLayout cardLayout;
    private JPanel cardContainer;

    private ProductPanel productPanel;
    private ProductDetailsPanel productDetailsPanel;
    private CartPanel cartPanel;
    private WishlistPanel wishlistPanel;
    private CheckoutPanel checkoutPanel;
    private OrderHistoryPanel orderHistoryPanel;
    private AdminPanel adminPanel;

    private JLabel cartCountBadge;
    private JLabel wishlistCountBadge;
    private JLabel userGreetingLabel;
    private JLabel roleBadgeLabel;
    private JLabel sidebarNameLabel;
    private JLabel sidebarEmailLabel;
    private JLabel toastLabel;
    private Timer toastTimer;

    private final List<JButton> navButtons = new ArrayList<>();
    private String currentCard = CARD_CATALOGUE;

    public HomeFrame(DataManager dataManager, UserManager userManager, ProductManager productManager,
                     CartManager cartManager, WishlistManager wishlistManager, OrderManager orderManager,
                     DiscountService discountService, BillingService billingService, InvoiceGenerator invoiceGenerator) {
        this.dataManager = dataManager;
        this.userManager = userManager;
        this.productManager = productManager;
        this.cartManager = cartManager;
        this.wishlistManager = wishlistManager;
        this.orderManager = orderManager;
        this.discountService = discountService;
        this.billingService = billingService;
        this.invoiceGenerator = invoiceGenerator;

        setTitle("Maisonelle – A little luxury, thoughtfully curated.");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1040, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Left Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Center Content Area: Top Navigation Bar + CardLayout Container
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Theme.MAIN_BACKGROUND);
        mainContent.add(createTopNavBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        productPanel = new ProductPanel(productManager, cartManager, wishlistManager, this);
        productDetailsPanel = new ProductDetailsPanel(cartManager, wishlistManager, this);
        cartPanel = new CartPanel(cartManager, billingService, this);
        wishlistPanel = new WishlistPanel(wishlistManager, cartManager, this);
        orderHistoryPanel = new OrderHistoryPanel(orderManager, userManager, this);
        adminPanel = new AdminPanel(productManager, orderManager, this);

        cardContainer.add(productPanel, CARD_CATALOGUE);
        cardContainer.add(productDetailsPanel, CARD_DETAILS);
        cardContainer.add(cartPanel, CARD_CART);
        cardContainer.add(wishlistPanel, CARD_WISHLIST);
        cardContainer.add(orderHistoryPanel, CARD_ORDERS);
        cardContainer.add(adminPanel, CARD_ADMIN);

        mainContent.add(cardContainer, BorderLayout.CENTER);

        // Toast Feedback Notification Label (South)
        toastLabel = new JLabel(" ", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getText() != null && !getText().trim().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.DARK_TEXT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        toastLabel.setFont(Theme.FONT_BODY_BOLD);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setBorder(new EmptyBorder(6, 16, 6, 16));
        toastLabel.setOpaque(false);

        JPanel toastWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toastWrapper.setOpaque(false);
        toastWrapper.add(toastLabel);
        mainContent.add(toastWrapper, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        updateCartBadge();
        updateWishlistBadge();
        showCatalogue();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, Short.MAX_VALUE));
        sidebar.setBackground(Theme.SECONDARY_BACKGROUND);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDERS));

        // Brand Logo Panel (North)
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(26, 20, 20, 20));

        JLabel brandTitle = new JLabel("M A I S O N E L L E");
        brandTitle.setFont(Theme.FONT_TITLE);
        brandTitle.setForeground(Theme.DARK_TEXT);
        brandPanel.add(brandTitle);

        brandPanel.add(Box.createVerticalStrut(4));

        JLabel brandSub = new JLabel("Thoughtfully Curated");
        brandSub.setFont(Theme.FONT_SMALL);
        brandSub.setForeground(Theme.MUTED_TEXT);
        brandPanel.add(brandSub);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Links (Center)
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 12, 10, 12));

        JButton catBtn = createNavButton("🛍️  Catalogue", CARD_CATALOGUE, this::showCatalogue);
        JButton wishBtn = createNavButton("🤍  Saved Wishlist", CARD_WISHLIST, this::showWishlist);
        JButton cartBtn = createNavButton("🛒  Shopping Bag", CARD_CART, this::showCart);
        JButton ordersBtn = createNavButton("📦  Order History", CARD_ORDERS, this::showOrderHistory);
        JButton adminBtn = createNavButton("📊  Admin Dashboard", CARD_ADMIN, this::showAdmin);

        navList.add(catBtn);
        navList.add(Box.createVerticalStrut(6));
        navList.add(wishBtn);
        navList.add(Box.createVerticalStrut(6));
        navList.add(cartBtn);
        navList.add(Box.createVerticalStrut(6));
        navList.add(ordersBtn);
        navList.add(Box.createVerticalStrut(6));
        navList.add(adminBtn);

        sidebar.add(navList, BorderLayout.CENTER);

        // User Profile & Sign Out (South)
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(16, 16, 24, 16));

        Customer currentUser = userManager.getCurrentUser();
        String uName = currentUser != null ? currentUser.getFullName() : "Guest";
        String uEmail = currentUser != null ? currentUser.getEmail() : "";

        sidebarNameLabel = new JLabel("👤 " + uName);
        sidebarNameLabel.setFont(Theme.FONT_BODY_BOLD);
        sidebarNameLabel.setForeground(Theme.DARK_TEXT);
        userPanel.add(sidebarNameLabel);

        userPanel.add(Box.createVerticalStrut(2));

        sidebarEmailLabel = new JLabel(uEmail);
        sidebarEmailLabel.setFont(Theme.FONT_SMALL);
        sidebarEmailLabel.setForeground(Theme.MUTED_TEXT);
        userPanel.add(sidebarEmailLabel);

        userPanel.add(Box.createVerticalStrut(14));

        JButton logoutBtn = Theme.createSecondaryButton("🚪 Sign Out");
        logoutBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        logoutBtn.addActionListener(e -> handleLogout());
        userPanel.add(logoutBtn);

        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName, Runnable onClick) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = currentCard.equals(cardName);
                if (isActive) {
                    g2.setColor(Theme.PRIMARY_BUTTON);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.SOFT_ACCENT);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        btn.addActionListener(e -> {
            onClick.run();
            highlightNavButton(cardName);
        });

        navButtons.add(btn);
        return btn;
    }

    private void highlightNavButton(String cardName) {
        currentCard = cardName;
        for (JButton b : navButtons) {
            boolean active = (currentCard.equals(CARD_CATALOGUE) && b.getText().contains("Catalogue"))
                    || (currentCard.equals(CARD_WISHLIST) && b.getText().contains("Wishlist"))
                    || (currentCard.equals(CARD_CART) && b.getText().contains("Bag"))
                    || (currentCard.equals(CARD_ORDERS) && b.getText().contains("Order History"))
                    || (currentCard.equals(CARD_ADMIN) && b.getText().contains("Admin"));
            b.setForeground(active ? Color.WHITE : Theme.DARK_TEXT);
            b.repaint();
        }
    }

    private JPanel createTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.decode("#FAF7F2"));
        nav.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDERS),
                new EmptyBorder(12, 28, 12, 28)
        ));

        // Left Greeting
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        Customer user = userManager.getCurrentUser();
        String name = user != null ? user.getFullName() : "Guest";
        userGreetingLabel = new JLabel("Welcome back, " + name);
        userGreetingLabel.setFont(Theme.FONT_HEADER);
        userGreetingLabel.setForeground(Theme.DARK_TEXT);
        left.add(userGreetingLabel);

        boolean isAdmin = user != null && user.isAdmin();
        roleBadgeLabel = Theme.createBadge(
                isAdmin ? "ADMINISTRATOR" : "CUSTOMER",
                isAdmin ? Theme.SECONDARY_BACKGROUND : new Color(94, 126, 99, 30),
                isAdmin ? Theme.MAIN_HEADINGS : Theme.SUCCESS
        );
        left.add(roleBadgeLabel);
        nav.add(left, BorderLayout.WEST);

        // Right Quick Actions (Wishlist & Cart Shortcuts)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JButton wishNavBtn = Theme.createOutlineButton("🤍 Wishlist");
        wishNavBtn.addActionListener(e -> showWishlist());
        wishlistCountBadge = Theme.createBadge("0", Theme.SECONDARY_BACKGROUND, Theme.DARK_TEXT);

        JPanel wishWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        wishWrapper.setOpaque(false);
        wishWrapper.add(wishNavBtn);
        wishWrapper.add(wishlistCountBadge);
        right.add(wishWrapper);

        JButton cartNavBtn = Theme.createPrimaryButton("🛍️ Bag");
        cartNavBtn.addActionListener(e -> showCart());
        cartCountBadge = Theme.createBadge("0", Theme.SOFT_ACCENT, Theme.DARK_TEXT);

        JPanel cartWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        cartWrapper.setOpaque(false);
        cartWrapper.add(cartNavBtn);
        cartWrapper.add(cartCountBadge);
        right.add(cartWrapper);

        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    public void updateCartBadge() {
        if (cartCountBadge != null) {
            cartCountBadge.setText(String.valueOf(cartManager.getTotalItemCount()));
        }
    }

    public void updateWishlistBadge() {
        if (wishlistCountBadge != null && wishlistManager != null) {
            wishlistCountBadge.setText(String.valueOf(wishlistManager.getWishlistCount()));
        }
    }

    public void updateUserDisplay() {
        Customer user = userManager.getCurrentUser();
        String name = user != null ? user.getFullName() : "Guest";
        String email = user != null ? user.getEmail() : "";
        boolean isAdmin = user != null && user.isAdmin();

        if (sidebarNameLabel != null) sidebarNameLabel.setText("👤 " + name);
        if (sidebarEmailLabel != null) sidebarEmailLabel.setText(email);
        if (userGreetingLabel != null) userGreetingLabel.setText("Welcome back, " + name);
        if (roleBadgeLabel != null) {
            roleBadgeLabel.setText(isAdmin ? "ADMINISTRATOR" : "CUSTOMER");
            roleBadgeLabel.setBackground(isAdmin ? Theme.SECONDARY_BACKGROUND : new Color(94, 126, 99, 30));
            roleBadgeLabel.setForeground(isAdmin ? Theme.MAIN_HEADINGS : Theme.SUCCESS);
        }
    }

    public void refreshCatalogue() {
        if (productPanel != null) {
            productPanel.refreshProducts();
        }
    }

    public void showCatalogue() {
        highlightNavButton(CARD_CATALOGUE);
        productPanel.refreshProducts();
        cardLayout.show(cardContainer, CARD_CATALOGUE);
    }

    public void showProductDetails(Product p) {
        highlightNavButton(CARD_DETAILS);
        productDetailsPanel.setProduct(p);
        cardLayout.show(cardContainer, CARD_DETAILS);
    }

    public void showWishlist() {
        highlightNavButton(CARD_WISHLIST);
        wishlistPanel.refreshWishlist();
        cardLayout.show(cardContainer, CARD_WISHLIST);
    }

    public void showCart() {
        highlightNavButton(CARD_CART);
        cartPanel.refreshCart();
        cardLayout.show(cardContainer, CARD_CART);
    }

    public void showCheckout() {
        highlightNavButton(CARD_CHECKOUT);
        checkoutPanel = new CheckoutPanel(cartManager, orderManager, userManager, billingService, discountService, this);
        cardContainer.add(checkoutPanel, CARD_CHECKOUT);
        cardLayout.show(cardContainer, CARD_CHECKOUT);
    }

    public void showOrderHistory() {
        highlightNavButton(CARD_ORDERS);
        orderHistoryPanel.refreshOrders();
        cardLayout.show(cardContainer, CARD_ORDERS);
    }

    public void showAdmin() {
        Customer u = userManager.getCurrentUser();
        if (u == null || !u.isAdmin()) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Administrator privileges are required to access this dashboard.\n\n" +
                    "Currently signed in as: " + (u != null ? u.getFullName() + " (" + u.getEmail() + ")" : "Guest") + "\n\n" +
                    "Would you like to switch to the Administrator Account (admin@maisonelle.com)?",
                    "Admin Privileges Required",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"Switch to Admin Account", "Cancel"},
                    "Switch to Admin Account"
            );

            if (choice == 0) {
                try {
                    Customer admin = userManager.authenticate("admin@maisonelle.com", "admin123");
                    if (admin != null) {
                        updateUserDisplay();
                        showToast("Switched to Administrator Mode");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to authenticate administrator account: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }

        highlightNavButton(CARD_ADMIN);
        adminPanel.refreshDashboard();
        cardLayout.show(cardContainer, CARD_ADMIN);
    }

    public void showToast(String message) {
        toastLabel.setText("  ✓  " + message + "  ");
        toastLabel.setOpaque(true);
        toastLabel.repaint();

        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }

        toastTimer = new Timer(3000, e -> {
            toastLabel.setText(" ");
            toastLabel.setOpaque(false);
            toastLabel.repaint();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to sign out of Maisonelle?",
                "Sign Out Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            userManager.logout();
            dispose();
            LoginFrame login = new LoginFrame(dataManager, userManager, productManager,
                    cartManager, wishlistManager, orderManager, discountService, billingService, invoiceGenerator);
            login.setVisible(true);
        }
    }
}
