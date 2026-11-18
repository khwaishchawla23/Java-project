package com.maisonelle;

import com.maisonelle.console.ConsoleUI;

/**
 * Main Application Entry Point for Maisonelle.
 * Terminal-based Luxury E-Commerce Application.
 *
 * Strictly Java 21 Standard Built-in JDK Only.
 * Zero Swing, Zero AWT GUI, Zero External Dependencies.
 */
public class Main {

    public static void main(String[] args) {
        try {
            ConsoleUI consoleUI = new ConsoleUI();
            consoleUI.start();
        } catch (Exception e) {
            System.err.println("Fatal Application Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
