package com.infobeans.consumerfinance.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
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
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${encryption.key:${ENCRYPTION_KEY:defaultEncryptionKey1234567890123456}}")
    private String encryptionKey;

    private SecretKey secretKey;

    /**
     * Initialize the encryption service with the configured key.
     * This method is called after dependency injection.
     */
    @PostConstruct
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
     * Encrypt a plaintext string using AES-GCM.
     * GCM mode provides both confidentiality and authenticity.
     *
     * @param plaintext the text to encrypt
     * @return Base64-encoded encrypted text (IV + ciphertext + tag), or null if plaintext is null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            if (secretKey == null) {
                initializeKey();
            }

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            return Base64.encodeBase64String(byteBuffer.array());
        } catch (Exception e) {
            LOGGER.severe("Encryption failed: " + e.getMessage());
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt a Base64-encoded encrypted string using AES-GCM.
     *
     * @param encryptedText Base64-encoded encrypted text (IV + ciphertext + tag)
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

            byte[] decodedBytes = Base64.decodeBase64(encryptedText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            // Extract encrypted data
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);
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
