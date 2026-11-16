package com.maisonelle.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Centralized design system for Maisonelle.
 * Implements the pastel brown, beige, latte, and mocha aesthetic using pure Java Swing & AWT.
 * Strictly no third-party libraries.
 */
public class Theme {

    // 1. Color Palette
    public static final Color MAIN_BACKGROUND      = Color.decode("#F7F2EA"); // Vanilla Cream
    public static final Color SECONDARY_BACKGROUND = Color.decode("#EDE2D3"); // Soft Beige
    public static final Color CARD_BACKGROUND      = Color.decode("#FFFCF8"); // Warm Ivory
    public static final Color CARD_WHITE           = Color.decode("#FFFFFF"); // Pure White
    public static final Color SOFT_ACCENT          = Color.decode("#D8C3AD"); // Oat Latte
    public static final Color SECONDARY_ACCENT     = Color.decode("#C5AD98"); // Dusty Taupe
    public static final Color PRIMARY_BUTTON       = Color.decode("#B99B80"); // Mocha Brown
    public static final Color PRIMARY_BUTTON_HOVER = Color.decode("#A8896E"); // Deep Mocha
    public static final Color MAIN_HEADINGS        = Color.decode("#8C6D56"); // Cocoa Brown
    public static final Color DARK_TEXT            = Color.decode("#594438"); // Espresso
    public static final Color BORDERS              = Color.decode("#E4D7C8"); // Sand Beige
    public static final Color MUTED_TEXT           = Color.decode("#9B8878"); // Warm Grey Brown
    public static final Color SUCCESS              = Color.decode("#5E7E63"); // Sage Green
    public static final Color DANGER               = Color.decode("#B55A52"); // Muted Terracotta

    // 2. Typography
    public static final Font FONT_DISPLAY    = new Font("Serif", Font.BOLD, 26);
    public static final Font FONT_TITLE      = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_HEADER     = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE   = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD  = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL      = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("SansSerif", Font.BOLD, 11);

    private static final DecimalFormat RUPEE_FORMAT = new DecimalFormat("₹#,##0.00");

    public static String formatRupee(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        return RUPEE_FORMAT.format(amount);
    }

    // 3. UI Component Factory Methods

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_BUTTON.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_BUTTON_HOVER);
                } else {
                    g2.setColor(PRIMARY_BUTTON);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(SECONDARY_BACKGROUND.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(SOFT_ACCENT);
                } else {
                    g2.setColor(SECONDARY_BACKGROUND);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(DARK_TEXT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(237, 226, 211, 180));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.setColor(PRIMARY_BUTTON);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(DARK_TEXT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? DANGER.darker() : DANGER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BACKGROUND);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(BORDERS);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setForeground(DARK_TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(DARK_TEXT);
        Border line = new LineBorder(BORDERS, 1, true);
        Border pad = new EmptyBorder(8, 12, 8, 12);
        field.setBorder(new CompoundBorder(line, pad));
        return field;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setForeground(DARK_TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(DARK_TEXT);
        Border line = new LineBorder(BORDERS, 1, true);
        Border pad = new EmptyBorder(8, 12, 8, 12);
        field.setBorder(new CompoundBorder(line, pad));
        return field;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL_BOLD);
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
