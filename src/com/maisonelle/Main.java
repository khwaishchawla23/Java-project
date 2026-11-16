package com.maisonelle;

import com.maisonelle.manager.*;
import com.maisonelle.service.BillingService;
import com.maisonelle.service.DiscountService;
import com.maisonelle.ui.LoginFrame;
import com.maisonelle.util.DataManager;
import com.maisonelle.util.InvoiceGenerator;

import javax.swing.*;
import java.io.File;

/**
 * Main Application Entry Point for Maisonelle.
 * Initializes core business managers, local data persistence, and launches
 * the Swing graphical interface on the Event Dispatch Thread (EDT).
 *
 * Strictly Java 21 Standard Built-in JDK Only.
 */
public class Main {

    public static void main(String[] args) {
        // Configure standard system UI properties
        System.setProperty("apple.awt.application.name", "Maisonelle");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize Data Management & Storage
                DataManager dataManager = new DataManager();
                File invoiceDir = dataManager.getInvoiceDir();
                InvoiceGenerator invoiceGenerator = new InvoiceGenerator(invoiceDir);

                // Initialize Domain Managers & Business Services
                ProductManager productManager = new ProductManager(dataManager);
                UserManager userManager = new UserManager(dataManager);
                CartManager cartManager = new CartManager();
                WishlistManager wishlistManager = new WishlistManager(dataManager, productManager);
                OrderManager orderManager = new OrderManager(dataManager, productManager, invoiceGenerator);
                DiscountService discountService = new DiscountService(dataManager);
                BillingService billingService = new BillingService(discountService);

                // Launch Authentication Screen
                LoginFrame loginFrame = new LoginFrame(
                        dataManager,
                        userManager,
                        productManager,
                        cartManager,
                        wishlistManager,
                        orderManager,
                        discountService,
                        billingService,
                        invoiceGenerator
                );
                loginFrame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Maisonelle: " + e.getMessage(),
                        "Initialization Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
