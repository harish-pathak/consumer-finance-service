package com.infobeans.consumerfinance.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Utility class for UUID operations.
 * Provides methods for generating, validating, and converting UUIDs.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public final class UUIDUtil {

    private UUIDUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a random UUID.
     *
     * @return UUID instance
     */
    public static UUID generateUUID() {
        return UUID.randomUUID();
    }

    /**
     * Converts a UUID to a byte array.
     *
     * @param uuid the UUID to convert
     * @return byte array representation
     */
    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    /**
     * Converts a byte array to a UUID.
     *
     * @param bytes the byte array
     * @return UUID instance
     */
    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Invalid byte array for UUID conversion");
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    /**
     * Validates if a string is a valid UUID.
     *
     * @param uuidString the string to validate
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValidUUID(String uuidString) {
        if (uuidString == null || uuidString.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Safely parses a UUID from string.
     *
     * @param uuidString the string to parse
     * @return UUID instance or null if invalid
     */
    public static UUID safeFromString(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Converts UUID to string without dashes.
     *
     * @param uuid the UUID
     * @return string representation without dashes
     */
    public static String toStringWithoutDashes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString().replace("-", "");
    }

    /**
     * Parses UUID from string without dashes.
     *
     * @param uuidString the string without dashes
     * @return UUID instance
     */
    public static UUID fromStringWithoutDashes(String uuidString) {
        if (uuidString == null || uuidString.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string format");
        }
        String formatted = String.format("%s-%s-%s-%s-%s",
                uuidString.substring(0, 8),
                uuidString.substring(8, 12),
                uuidString.substring(12, 16),
                uuidString.substring(16, 20),
                uuidString.substring(20, 32));
        return UUID.fromString(formatted);
    }

    /**
     * Generates a deterministic UUID from a string seed.
     * Uses UUID version 3 (MD5 hash).
     *
     * @param seed the seed string
     * @return UUID instance
     */
    public static UUID generateFromString(String seed) {
        if (seed == null) {
            throw new IllegalArgumentException("Seed cannot be null");
        }
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }

    /**
     * Checks if two UUIDs are equal.
     *
     * @param uuid1 first UUID
     * @param uuid2 second UUID
     * @return true if equal, false otherwise
     */
    public static boolean equals(UUID uuid1, UUID uuid2) {
        if (uuid1 == null && uuid2 == null) {
            return true;
        }
        if (uuid1 == null || uuid2 == null) {
            return false;
        }
        return uuid1.equals(uuid2);
    }
}
