package com.maisonelle.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

/**
 * Standard Java Built-in Cryptography Utility for secure password hashing.
 * Implements PBKDF2 with HMAC-SHA256 and unique per-user cryptographic salts.
 * Strictly no third-party libraries.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically secure random salt hex string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Hashes a plain password using PBKDF2 with the provided salt.
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt cannot be null.");
        }
        try {
            byte[] salt = HexFormat.of().parseHex(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Fallback to SHA-256 if PBKDF2 provider is restricted in specific minimal runtime
            return sha256Fallback(password, saltHex);
        }
    }

    /**
     * Verifies that the provided plain password matches the expected hash.
     */
    public static boolean verifyPassword(String password, String expectedHash, String saltHex) {
        if (password == null || expectedHash == null || saltHex == null) {
            return false;
        }
        String computed = hashPassword(password, saltHex);
        return computed.equalsIgnoreCase(expectedHash);
    }

    private static String sha256Fallback(String password, String saltHex) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            md.update(saltHex.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] bytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new RuntimeException("Cryptographic digest unavailable", ex);
        }
    }
}
