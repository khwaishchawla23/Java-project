package com.maisonelle.console;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Robust terminal input utility.
 * Prevents Scanner errors, infinite loops, and token mismatches.
 * Strictly Java 21 Standard JDK Built-ins only.
 */
public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public InputHelper() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reads an integer within [min, max] range.
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("  [!] Input cannot be empty. Please enter a number between " + min + " and " + max + ".");
                continue;
            }
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("  [!] Choice must be between " + min + " and " + max + ". Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads an integer with an optional default value when empty.
     */
    public int readIntWithDefault(String prompt, int defaultValue, int min, int max) {
        while (true) {
            System.out.print(prompt + " [Default: " + defaultValue + "]: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("  [!] Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads a non-empty string or optional string.
     */
    public String readString(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!allowEmpty && line.isEmpty()) {
                System.out.println("  [!] This field cannot be empty. Please enter a value.");
                continue;
            }
            return line;
        }
    }

    /**
     * Reads a string with a default value.
     */
    public String readStringWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt + " [Current: " + defaultValue + "]: ");
        String line = scanner.nextLine().trim();
        return line.isEmpty() ? defaultValue : line;
    }

    /**
     * Reads a BigDecimal monetary amount with minimum value validation.
     */
    public BigDecimal readBigDecimal(String prompt, BigDecimal min) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace("₹", "").replace(",", "");
            if (line.isEmpty()) {
                System.out.println("  [!] Amount cannot be empty.");
                continue;
            }
            try {
                BigDecimal val = new BigDecimal(line);
                if (min != null && val.compareTo(min) < 0) {
                    System.out.println("  [!] Amount cannot be less than " + min + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid monetary format. Please enter a valid number (e.g. 1499.00).");
            }
        }
    }

    /**
     * Reads a BigDecimal with a default value.
     */
    public BigDecimal readBigDecimalWithDefault(String prompt, BigDecimal defaultValue, BigDecimal min) {
        while (true) {
            System.out.print(prompt + " [Current: ₹" + defaultValue + "]: ");
            String line = scanner.nextLine().trim().replace("₹", "").replace(",", "");
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                BigDecimal val = new BigDecimal(line);
                if (min != null && val.compareTo(min) < 0) {
                    System.out.println("  [!] Amount cannot be less than " + min + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid monetary format.");
            }
        }
    }

    /**
     * Reads password securely using System.console() when available,
     * otherwise falling back to scanner.
     */
    public String readPassword(String prompt) {
        if (System.console() != null) {
            char[] chars = System.console().readPassword(prompt);
            if (chars != null) {
                return new String(chars).trim();
            }
        }
        return readString(prompt, false);
    }

    /**
     * Confirms a Yes/No action.
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("  [!] Please enter 'y' for yes or 'n' for no.");
        }
    }

    /**
     * Pauses the console until user presses Enter.
     */
    public void pause() {
        System.out.print("\nPress [Enter] to continue...");
        scanner.nextLine();
    }
}
