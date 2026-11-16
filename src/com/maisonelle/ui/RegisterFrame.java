package com.maisonelle.ui;

import com.maisonelle.manager.*;
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
 * Clean, elegant user registration screen with client-side validation
 * and cryptographic hashing.
 */
public class RegisterFrame extends JFrame {

    private final DataManager dataManager;
    private final UserManager userManager;
    private final ProductManager productManager;
    private final CartManager cartManager;
    private final WishlistManager wishlistManager;
    private final OrderManager orderManager;
    private final DiscountService discountService;
    private final BillingService billingService;
    private final InvoiceGenerator invoiceGenerator;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField pincodeField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;
    private JLabel errorLabel;

    public RegisterFrame(DataManager dataManager, UserManager userManager, ProductManager productManager,
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

        setTitle("Maisonelle – Create Your Boutique Account");
        setSize(480, 720);
        setMinimumSize(new Dimension(460, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.MAIN_BACKGROUND);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 36, 24, 36));

        // Header Panel
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel brandLabel = new JLabel("M A I S O N E L L E");
        brandLabel.setFont(Theme.FONT_TITLE);
        brandLabel.setForeground(Theme.MAIN_HEADINGS);
        brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(brandLabel);

        header.add(Box.createVerticalStrut(4));

        JLabel subLabel = new JLabel("Join our curated lifestyle collective.");
        subLabel.setFont(Theme.FONT_BODY);
        subLabel.setForeground(Theme.MUTED_TEXT);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subLabel);

        header.add(Box.createVerticalStrut(14));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL_BOLD);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(errorLabel);

        root.add(header, BorderLayout.NORTH);

        // Form Card Panel
        JPanel card = new JPanel() {
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
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        nameField = Theme.createTextField(20);
        emailField = Theme.createTextField(20);
        phoneField = Theme.createTextField(20);
        addressField = Theme.createTextField(20);
        cityField = Theme.createTextField(20);
        pincodeField = Theme.createTextField(20);
        passField = Theme.createPasswordField(20);
        confirmPassField = Theme.createPasswordField(20);

        addFormField(card, "Full Legal Name", nameField);
        addFormField(card, "Email Address", emailField);
        addFormField(card, "Phone Number", phoneField);
        addFormField(card, "Delivery Address", addressField);

        JPanel cityPinRow = new JPanel(new GridLayout(1, 2, 10, 0));
        cityPinRow.setOpaque(false);
        JPanel cityCol = new JPanel(); cityCol.setLayout(new BoxLayout(cityCol, BoxLayout.Y_AXIS)); cityCol.setOpaque(false);
        JLabel cityLbl = new JLabel("City"); cityLbl.setFont(Theme.FONT_SMALL_BOLD); cityLbl.setForeground(Theme.DARK_TEXT);
        cityCol.add(cityLbl); cityCol.add(Box.createVerticalStrut(4)); cityCol.add(cityField);
        JPanel pinCol = new JPanel(); pinCol.setLayout(new BoxLayout(pinCol, BoxLayout.Y_AXIS)); pinCol.setOpaque(false);
        JLabel pinLbl = new JLabel("PIN Code"); pinLbl.setFont(Theme.FONT_SMALL_BOLD); pinLbl.setForeground(Theme.DARK_TEXT);
        pinCol.add(pinLbl); pinCol.add(Box.createVerticalStrut(4)); pinCol.add(pincodeField);
        cityPinRow.add(cityCol); cityPinRow.add(pinCol);
        card.add(cityPinRow);
        card.add(Box.createVerticalStrut(10));

        addFormField(card, "Password (min. 6 characters)", passField);
        addFormField(card, "Confirm Password", confirmPassField);

        card.add(Box.createVerticalStrut(10));

        JButton registerBtn = Theme.createPrimaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> handleRegister());
        card.add(registerBtn);

        card.add(Box.createVerticalStrut(12));

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backRow.setOpaque(false);
        JLabel hasAcc = new JLabel("Already have an account? ");
        hasAcc.setFont(Theme.FONT_BODY);
        hasAcc.setForeground(Theme.MUTED_TEXT);
        backRow.add(hasAcc);

        JButton loginLink = new JButton("Sign In");
        loginLink.setFont(Theme.FONT_BODY_BOLD);
        loginLink.setForeground(Theme.MAIN_HEADINGS);
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> openLoginScreen(null));
        backRow.add(loginLink);

        card.add(backRow);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
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

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String pin = pincodeField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            showError("Please complete all required fields.");
            return;
        }

        if (!pass.equals(confirm)) {
            showError("Passwords do not match. Please re-enter.");
            confirmPassField.setText("");
            confirmPassField.requestFocusInWindow();
            return;
        }

        try {
            userManager.register(name, email, phone, address, city, pin, pass);
            JOptionPane.showMessageDialog(this,
                    "Welcome to Maisonelle, " + name + "!\nYour account was created successfully.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            openLoginScreen(email);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void openLoginScreen(String prefilledEmail) {
        dispose();
        LoginFrame login = new LoginFrame(dataManager, userManager, productManager,
                cartManager, wishlistManager, orderManager, discountService, billingService, invoiceGenerator);
        if (prefilledEmail != null) {
            login.setEmail(prefilledEmail);
        }
        login.setVisible(true);
    }
}
