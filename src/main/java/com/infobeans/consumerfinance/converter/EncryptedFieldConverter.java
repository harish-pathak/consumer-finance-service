package com.infobeans.consumerfinance.converter;

import com.infobeans.consumerfinance.util.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA AttributeConverter for automatic encryption/decryption of sensitive fields.
 *
 * Usage: Add @Convert(converter = EncryptedFieldConverter.class) annotation to entity fields
 * that should be encrypted before persistence and decrypted on retrieval.
 *
 * Example:
 * @Column(name = "national_id")
 * @Convert(converter = EncryptedFieldConverter.class)
 * private String nationalId;
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Converter(autoApply = false)
public class EncryptedFieldConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    /**
     * Inject the EncryptionService using static setter.
     * This workaround is needed because Spring cannot inject beans into JPA converters directly.
     */
    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptedFieldConverter.encryptionService = encryptionService;
    }

    /**
     * Convert entity attribute (plaintext) to database column value (encrypted).
     * Called before persisting the entity.
     *
     * @param attribute the plaintext attribute value
     * @return encrypted Base64-encoded value
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        if (encryptionService == null) {
            throw new RuntimeException("EncryptionService is not initialized");
        }
        return encryptionService.encrypt(attribute);
    }

    /**
     * Convert database column value (encrypted) to entity attribute (plaintext).
     * Called after retrieving the entity from the database.
     *
     * @param dbData the encrypted Base64-encoded database value
     * @return decrypted plaintext value
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        if (encryptionService == null) {
            throw new RuntimeException("EncryptionService is not initialized");
        }
        return encryptionService.decrypt(dbData);
    }
}
