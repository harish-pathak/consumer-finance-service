package com.infobeans.consumerfinance.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * JPA Converter for UUID to String conversion.
 *
 * Converts UUID Java type to VARCHAR/CHAR string representation in the database.
 * This ensures UUIDs are stored as 36-character strings (with dashes) in CHAR(36) columns.
 *
 * Usage: Add @Convert(converter = UUIDConverter.class) to UUID entity fields.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    /**
     * Converts UUID entity attribute to database column value (String).
     *
     * @param uuid the UUID entity attribute
     * @return String representation of UUID (36 chars with dashes), or null if input is null
     */
    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }

    /**
     * Converts database column value (String) to UUID entity attribute.
     *
     * @param dbData the database column value as String
     * @return UUID entity attribute, or null if input is null
     */
    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format in database: " + dbData, e);
        }
    }
}
