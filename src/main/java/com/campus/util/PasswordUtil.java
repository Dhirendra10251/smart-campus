package com.campus.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil — Provides salted SHA-256 password hashing.
 *
 * Format stored in DB:  BASE64(salt):BASE64(SHA-256(salt + password))
 *
 * Note: For a production application, BCrypt or Argon2 would be preferred.
 * SHA-256 with a random salt is used here because it relies only on the
 * standard Java cryptography API (no external dependency required).
 *
 * This is explicitly documented as a project-level implementation, not
 * production-grade security.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int    SALT_BYTES = 16;
    private static final String SEPARATOR  = ":";

    /**
     * Hashes a plain-text password using a freshly generated random salt.
     *
     * @param plainPassword the user's plain-text password
     * @return              stored hash string in format "salt:hash"
     */
    public static String hashPassword(String plainPassword) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);

            // Hash: SHA-256(salt || password)
            byte[] hash = computeHash(salt, plainPassword);

            // Encode both as Base64 and combine
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);
            return saltB64 + SEPARATOR + hashB64;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available in this JVM", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash string.
     *
     * @param plainPassword  the user's entered password
     * @param storedHash     the value retrieved from the database
     * @return               true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(SEPARATOR, 2);
            if (parts.length != 2) return false;

            byte[] salt    = Base64.getDecoder().decode(parts[0]);
            byte[] stored  = Base64.getDecoder().decode(parts[1]);
            byte[] computed = computeHash(salt, plainPassword);

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(computed, stored);

        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            Logger.error("Password verification error: " + e.getMessage());
            return false;
        }
    }

    private static byte[] computeHash(byte[] salt, String password)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        md.update(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return md.digest();
    }
}
