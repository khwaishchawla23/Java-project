package com.maisonelle.ui;

import com.maisonelle.manager.*;
import com.maisonelle.model.Customer;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;
import com.maisonelle.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern luxury authentication screen for Maisonelle.
 * Supports manual credential input, quick demo evaluation pills,
 * and Enter key keyboard navigation.
 */
public class LoginFrame extends JFrame {

    private final DataManager dataManager;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final OrderManager orderManager;
    private final DiscountService discountService;
    private final BillingService billingService;
    private final InvoiceGenerator invoiceGenerator;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame(DataManager dataManager, UserManager userManager, ProductManager productManager,
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

        setTitle("Maisonelle – A Little Luxury, Thoughtfully Curated");
        setSize(880, 560);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridLayout(1, 2, 0, 0));
        container.setOpaque(false);

        // ------------------ LEFT EDITORIAL BRAND HERO ------------------
        JPanel leftHero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle warm gradient
                GradientPaint gp = new GradientPaint(0, 0, Theme.SECONDARY_BACKGROUND,
                        getWidth(), getHeight(), Theme.SOFT_ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftHero.setLayout(new BoxLayout(leftHero, BoxLayout.Y_AXIS));
        leftHero.setBorder(new EmptyBorder(50, 48, 50, 48));

        JLabel brandTitle = new JLabel("M A I S O N E L L E");
        brandTitle.setFont(Theme.FONT_DISPLAY);
        brandTitle.setForeground(Theme.DARK_TEXT);
        leftHero.add(brandTitle);

        leftHero.add(Box.createVerticalStrut(10));

        JLabel tagline = new JLabel("A little luxury, thoughtfully curated.");
        tagline.setFont(Theme.FONT_SUBTITLE);
        tagline.setForeground(Theme.MAIN_HEADINGS);
        leftHero.add(tagline);

        leftHero.add(Box.createVerticalStrut(30));

        addHeroQuote(leftHero, "“Find beauty in the everyday.”");
        addHeroQuote(leftHero, "“Thoughtfully selected. Beautifully priced.”");
        addHeroQuote(leftHero, "“Discover timeless pieces made for your lifestyle.”");

        leftHero.add(Box.createVerticalGlue());

        JLabel copyright = new JLabel("© 2026 Maisonelle Lifestyle Group • Pure Java 21");
        copyright.setFont(Theme.FONT_SMALL);
        copyright.setForeground(Theme.MUTED_TEXT);
        leftHero.add(copyright);

        container.add(leftHero);

        // ------------------ RIGHT LOGIN FORM ------------------
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new GridBagLayout());
        rightCol.setOpaque(false);
        rightCol.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Theme.BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(28, 30, 28, 30));
        formCard.setPreferredSize(new Dimension(360, 460));

        JLabel signInHeader = new JLabel("Welcome Back");
        signInHeader.setFont(Theme.FONT_TITLE);
        signInHeader.setForeground(Theme.DARK_TEXT);
        formCard.add(signInHeader);

        JLabel signInSub = new JLabel("Please sign in to access your boutique cart.");
        signInSub.setFont(Theme.FONT_SMALL);
        signInSub.setForeground(Theme.MUTED_TEXT);
        formCard.add(signInSub);

        formCard.add(Box.createVerticalStrut(16));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL_BOLD);
        errorLabel.setForeground(Theme.DANGER);
        formCard.add(errorLabel);

        formCard.add(Box.createVerticalStrut(8));

        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.FONT_SMALL_BOLD);
        emailLabel.setForeground(Theme.DARK_TEXT);
        formCard.add(emailLabel);
        formCard.add(Box.createVerticalStrut(4));

        emailField = Theme.createTextField(18);
        emailField.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        emailField.addActionListener(e -> passwordField.requestFocusInWindow());
        formCard.add(emailField);

        formCard.add(Box.createVerticalStrut(12));

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_SMALL_BOLD);
        passLabel.setForeground(Theme.DARK_TEXT);
        formCard.add(passLabel);
        formCard.add(Box.createVerticalStrut(4));

        passwordField = Theme.createPasswordField(18);
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        passwordField.addActionListener(e -> handleLogin());
        formCard.add(passwordField);

        formCard.add(Box.createVerticalStrut(6));

        // Show password toggle
        JCheckBox showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setFont(Theme.FONT_SMALL);
        showPassCheck.setForeground(Theme.MUTED_TEXT);
        showPassCheck.setOpaque(false);
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        formCard.add(showPassCheck);

        formCard.add(Box.createVerticalStrut(16));

        JButton loginBtn = Theme.createPrimaryButton("Sign In to Maisonelle");
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> handleLogin());
        formCard.add(loginBtn);

        formCard.add(Box.createVerticalStrut(14));

        // Demo shortcuts
        JPanel demoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        demoRow.setOpaque(false);
        JLabel demoLbl = new JLabel("Demo Access:");
        demoLbl.setFont(Theme.FONT_SMALL);
        demoLbl.setForeground(Theme.MUTED_TEXT);
        demoRow.add(demoLbl);

        JButton custDemo = Theme.createSecondaryButton("Customer");
        custDemo.setFont(Theme.FONT_SMALL_BOLD);
        custDemo.addActionListener(e -> {
            emailField.setText("clara@maisonelle.com");
            passwordField.setText("customer123");
            errorLabel.setText("Demo credentials filled. Click Sign In.");
            errorLabel.setForeground(Theme.SUCCESS);
        });
        demoRow.add(custDemo);

        JButton adminDemo = Theme.createSecondaryButton("Admin");
        adminDemo.setFont(Theme.FONT_SMALL_BOLD);
        adminDemo.addActionListener(e -> {
            emailField.setText("admin@maisonelle.com");
            passwordField.setText("admin123");
            errorLabel.setText("Admin credentials filled. Click Sign In.");
            errorLabel.setForeground(Theme.SUCCESS);
        });
        demoRow.add(adminDemo);

        formCard.add(demoRow);

        formCard.add(Box.createVerticalStrut(12));

        // Register link
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        regRow.setOpaque(false);
        JLabel noAcc = new JLabel("New to Maisonelle? ");
        noAcc.setFont(Theme.FONT_BODY);
        noAcc.setForeground(Theme.MUTED_TEXT);
        regRow.add(noAcc);

        JButton regLink = new JButton("Create Account");
        regLink.setFont(Theme.FONT_BODY_BOLD);
        regLink.setForeground(Theme.MAIN_HEADINGS);
        regLink.setContentAreaFilled(false);
        regLink.setBorderPainted(false);
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.addActionListener(e -> {
            dispose();
            RegisterFrame reg = new RegisterFrame(dataManager, userManager, productManager,
                    cartManager, wishlistManager, orderManager, discountService, billingService, invoiceGenerator);
            reg.setVisible(true);
        });
        regRow.add(regLink);

        formCard.add(regRow);

        rightCol.add(formCard);
        container.add(rightCol);

        add(container, BorderLayout.CENTER);
    }

    private void addHeroQuote(JPanel parent, String quote) {
        JLabel q = new JLabel(quote);
        q.setFont(Theme.FONT_BODY_BOLD);
        q.setForeground(Theme.DARK_TEXT);
        q.setBorder(new EmptyBorder(6, 0, 6, 0));
        parent.add(q);
    }

    public void setEmail(String email) {
        if (emailField != null) {
            emailField.setText(email);
            passwordField.requestFocusInWindow();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email address.");
            errorLabel.setForeground(Theme.DANGER);
            emailField.requestFocusInWindow();
            return;
        }

        if (pass.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            errorLabel.setForeground(Theme.DANGER);
            passwordField.requestFocusInWindow();
            return;
        }

        try {
            Customer user = userManager.authenticate(email, pass);
            dispose();

            HomeFrame home = new HomeFrame(dataManager, userManager, productManager,
                    cartManager, wishlistManager, orderManager, discountService, billingService, invoiceGenerator);
            home.setVisible(true);

            if (user != null && user.isAdmin()) {
                home.showAdmin();
            }

        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setForeground(Theme.DANGER);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
}
