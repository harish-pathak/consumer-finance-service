package com.infobeans.consumerfinance.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility class for date and time operations.
 * Provides methods for date calculations, formatting, and conversions.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public final class DateUtil {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets the current date.
     *
     * @return current LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Gets the current date and time.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Gets the current date and time in UTC.
     *
     * @return current UTC LocalDateTime
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Adds days to a date.
     *
     * @param date the date
     * @param days the number of days to add
     * @return new LocalDate
     */
    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * Adds months to a date.
     *
     * @param date   the date
     * @param months the number of months to add
     * @return new LocalDate
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    /**
     * Adds years to a date.
     *
     * @param date  the date
     * @param years the number of years to add
     * @return new LocalDate
     */
    public static LocalDate addYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    /**
     * Calculates the number of days between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return number of days
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates the number of months between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return number of months
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Calculates the number of years between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return number of years
     */
    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.YEARS.between(startDate, endDate);
    }

    /**
     * Checks if a date is in the past.
     *
     * @param date the date to check
     * @return true if in the past, false otherwise
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(today());
    }

    /**
     * Checks if a date is in the future.
     *
     * @param date the date to check
     * @return true if in the future, false otherwise
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(today());
    }

    /**
     * Checks if a date is today.
     *
     * @param date the date to check
     * @return true if today, false otherwise
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(today());
    }

    /**
     * Gets the start of the month for a date.
     *
     * @param date the date
     * @return first day of the month
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Gets the end of the month for a date.
     *
     * @param date the date
     * @return last day of the month
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Gets the start of the year for a date.
     *
     * @param date the date
     * @return first day of the year
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Gets the end of the year for a date.
     *
     * @param date the date
     * @return last day of the year
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Formats a date to ISO format (yyyy-MM-dd).
     *
     * @param date the date
     * @return formatted string
     */
    public static String formatIsoDate(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : null;
    }

    /**
     * Formats a date-time to ISO format (yyyy-MM-ddTHH:mm:ss).
     *
     * @param dateTime the date-time
     * @return formatted string
     */
    public static String formatIsoDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATETIME_FORMATTER) : null;
    }

    /**
     * Formats a date for display (dd-MMM-yyyy).
     *
     * @param date the date
     * @return formatted string
     */
    public static String formatDisplayDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : null;
    }

    /**
     * Formats a date-time for display (dd-MMM-yyyy HH:mm:ss).
     *
     * @param dateTime the date-time
     * @return formatted string
     */
    public static String formatDisplayDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : null;
    }

    /**
     * Parses a date from ISO format string (yyyy-MM-dd).
     *
     * @param dateString the date string
     * @return LocalDate instance
     */
    public static LocalDate parseIsoDate(String dateString) {
        return dateString != null ? LocalDate.parse(dateString, ISO_DATE_FORMATTER) : null;
    }

    /**
     * Parses a date-time from ISO format string (yyyy-MM-ddTHH:mm:ss).
     *
     * @param dateTimeString the date-time string
     * @return LocalDateTime instance
     */
    public static LocalDateTime parseIsoDateTime(String dateTimeString) {
        return dateTimeString != null ? LocalDateTime.parse(dateTimeString, ISO_DATETIME_FORMATTER) : null;
    }

    /**
     * Converts LocalDate to LocalDateTime at start of day.
     *
     * @param date the date
     * @return LocalDateTime at 00:00:00
     */
    public static LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Converts LocalDate to LocalDateTime at end of day.
     *
     * @param date the date
     * @return LocalDateTime at 23:59:59
     */
    public static LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59, 999_999_999) : null;
    }

    /**
     * Checks if a date falls within a range (inclusive).
     *
     * @param date      the date to check
     * @param startDate the start date
     * @param endDate   the end date
     * @return true if within range, false otherwise
     */
    public static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Gets the age in years from a birth date.
     *
     * @param birthDate the birth date
     * @return age in years
     */
    public static int calculateAge(LocalDate birthDate) {
        return birthDate != null ? Period.between(birthDate, today()).getYears() : 0;
    }

    /**
     * Checks if a person is of legal age (18 years or older).
     *
     * @param birthDate the birth date
     * @return true if 18 or older, false otherwise
     */
    public static boolean isLegalAge(LocalDate birthDate) {
        return calculateAge(birthDate) >= 18;
    }
}
