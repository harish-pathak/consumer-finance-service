package com.infobeans.consumerfinance.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * Provides common validation methods for business rules.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]+$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z\\s'-]+$"
    );

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates if a string is not null or empty.
     *
     * @param value the string to validate
     * @return true if not null or empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates if a string is null or empty.
     *
     * @param value the string to validate
     * @return true if null or empty, false otherwise
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates email format.
     *
     * @param email the email to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format.
     *
     * @param phone the phone number to validate
     * @return true if valid phone format, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        String cleaned = phone.replaceAll("[\\s()-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Validates if a string contains only alphanumeric characters.
     *
     * @param value the string to validate
     * @return true if alphanumeric, false otherwise
     */
    public static boolean isAlphanumeric(String value) {
        return isNotEmpty(value) && ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Validates if a string is a valid name (letters, spaces, hyphens, apostrophes).
     *
     * @param name the name to validate
     * @return true if valid name, false otherwise
     */
    public static boolean isValidName(String name) {
        return isNotEmpty(name) && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validates if a string length is within range.
     *
     * @param value     the string to validate
     * @param minLength minimum length
     * @param maxLength maximum length
     * @return true if length is within range, false otherwise
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates if a BigDecimal is positive.
     *
     * @param value the value to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Validates if a BigDecimal is non-negative (zero or positive).
     *
     * @param value the value to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validates if a BigDecimal is within range.
     *
     * @param value the value to validate
     * @param min   minimum value (inclusive)
     * @param max   maximum value (inclusive)
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) {
            return false;
        }
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    /**
     * Validates if a date is in the past.
     *
     * @param date the date to validate
     * @return true if in the past, false otherwise
     */
    public static boolean isInPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Validates if a date is in the future.
     *
     * @param date the date to validate
     * @return true if in the future, false otherwise
     */
    public static boolean isInFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Validates if a date is today or in the future.
     *
     * @param date the date to validate
     * @return true if today or future, false otherwise
     */
    public static boolean isTodayOrFuture(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Validates if a person is of legal age (18 years or older).
     *
     * @param birthDate the birth date
     * @return true if 18 or older, false otherwise
     */
    public static boolean isLegalAge(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        return DateUtil.calculateAge(birthDate) >= 18;
    }

    /**
     * Validates if age is within range.
     *
     * @param birthDate the birth date
     * @param minAge    minimum age
     * @param maxAge    maximum age
     * @return true if age is within range, false otherwise
     */
    public static boolean isAgeInRange(LocalDate birthDate, int minAge, int maxAge) {
        if (birthDate == null) {
            return false;
        }
        int age = DateUtil.calculateAge(birthDate);
        return age >= minAge && age <= maxAge;
    }

    /**
     * Validates if a number is positive.
     *
     * @param value the value to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    /**
     * Validates if a number is non-negative.
     *
     * @param value the value to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(Integer value) {
        return value != null && value >= 0;
    }

    /**
     * Validates if an integer is within range.
     *
     * @param value the value to validate
     * @param min   minimum value (inclusive)
     * @param max   maximum value (inclusive)
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(Integer value, int min, int max) {
        return value != null && value >= min && value <= max;
    }

    /**
     * Validates if a loan term (in months) is valid.
     *
     * @param termMonths the term in months
     * @param minMonths  minimum months allowed
     * @param maxMonths  maximum months allowed
     * @return true if valid, false otherwise
     */
    public static boolean isValidLoanTerm(Integer termMonths, int minMonths, int maxMonths) {
        return isInRange(termMonths, minMonths, maxMonths);
    }

    /**
     * Validates if an interest rate is valid (between 0 and 100).
     *
     * @param interestRate the interest rate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInterestRate(BigDecimal interestRate) {
        return isInRange(interestRate, BigDecimal.ZERO, new BigDecimal("100"));
    }

    /**
     * Sanitizes a string by trimming and removing extra spaces.
     *
     * @param value the string to sanitize
     * @return sanitized string
     */
    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Validates if a UUID string is valid.
     *
     * @param uuidString the UUID string
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValidUUID(String uuidString) {
        return UUIDUtil.isValidUUID(uuidString);
    }
}
