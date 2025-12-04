package com.infobeans.consumerfinance.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * Service for encrypting and decrypting sensitive data using AES encryption.
 * Uses Base64 encoding for storage in database columns.
 *
 * Sensitive fields that should be encrypted:
 * - national_id
 * - document_number
 * - employer_name
 * - monthly_income
 * - annual_income
 * - income_source
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
public class EncryptionService {

    private static final Logger LOGGER = Logger.getLogger(EncryptionService.class.getName());
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    @Value("${encryption.key:${ENCRYPTION_KEY:defaultEncryptionKey1234567890123456}}")
    private String encryptionKey;

    private SecretKey secretKey;

    /**
     * Initialize the encryption service with the configured key.
     * This method is called after dependency injection.
     */
    public void initializeKey() {
        try {
            byte[] decodedKey = Base64.decodeBase64(padKeyTo32Bytes(encryptionKey));
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ENCRYPTION_ALGORITHM);
            LOGGER.info("Encryption key initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize encryption key: " + e.getMessage());
            throw new RuntimeException("Failed to initialize encryption key", e);
        }
    }

    /**
     * Encrypt a plaintext string.
     *
     * @param plaintext the text to encrypt
     * @return Base64-encoded encrypted text, or null if plaintext is null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            if (secretKey == null) {
                initializeKey();
            }

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            LOGGER.severe("Encryption failed: " + e.getMessage());
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt a Base64-encoded encrypted string.
     *
     * @param encryptedText Base64-encoded encrypted text
     * @return decrypted plaintext, or null if encryptedText is null
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            if (secretKey == null) {
                initializeKey();
            }

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decodeBase64(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.severe("Decryption failed: " + e.getMessage());
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generate a new encryption key. Useful for key rotation.
     *
     * @return Base64-encoded encryption key
     */
    public String generateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey newKey = keyGenerator.generateKey();
            return Base64.encodeBase64String(newKey.getEncoded());
        } catch (Exception e) {
            LOGGER.severe("Key generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    /**
     * Pad the key to 32 bytes for AES-256 encryption.
     *
     * @param key the key to pad
     * @return padded key
     */
    private String padKeyTo32Bytes(String key) {
        if (key.length() >= 32) {
            return key.substring(0, 32);
        }
        return String.format("%-32s", key).replace(' ', '0');
    }
}
