package org.synyx.urlaubsverwaltung.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

public final class DateUtil {

    private static final int DAY_OF_NEW_YEARS_EVE = 31;
    private static final int DAY_OF_CHRISTMAS_EVE = 24;

    private DateUtil() {

        // Hide constructor for util classes
    }

    /**
     * Check if the given date is a work day.
     *
     * @param date to check
     * @return {@code true} if the given date is a work day, else {@code false}
     */
    public static boolean isWorkDay(LocalDate date) {

        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    /**
     * Check if given date is before April of the given year
     *
     * @param date to check
     * @param year the year whose April the date will be compared to
     * @return {@code true} if the given date is before April of the given year, {@code false} if it is in or after that April
     */
    public static boolean isBeforeApril(LocalDate date, int year) {

        return date.getMonth().getValue() < Month.APRIL.getValue() || date.getYear() < year;
    }

    /**
     * Check if given date is on Christmas Eve.
     *
     * @param date to check
     * @return {@code true} if the given date is on Christmas Eve, else {@code false}
     */
    public static boolean isChristmasEve(LocalDate date) {

        return date.getDayOfMonth() == DAY_OF_CHRISTMAS_EVE && date.getMonth() == Month.DECEMBER;
    }

    /**
     * Check if given date is on New Year's Eve.
     *
     * @param date to check
     * @return {@code true} if the given date is on New Year's Eve, else {@code false}
     */
    public static boolean isNewYearsEve(LocalDate date) {

        return date.getDayOfMonth() == DAY_OF_NEW_YEARS_EVE && date.getMonth() == Month.DECEMBER;
    }

    /**
     * Get the first day of the given year.
     *
     * @param year to get the first day of
     * @return the first day of the given year
     */
    public static LocalDate getFirstDayOfYear(int year) {

        return ZonedDateTime.now(UTC).withYear(year).with(firstDayOfYear()).toLocalDate();
    }

    /**
     * Get the last day of the given year.
     *
     * @param year to get the last day of
     * @return the last day of the given year
     */
    public static LocalDate getLastDayOfYear(int year) {

        return ZonedDateTime.now(UTC).withYear(year).with(lastDayOfYear()).toLocalDate();
    }

    /**
     * Get the first day of the given month in the given year.
     *
     * @param year  of the month to get the first day of
     * @param month to get the first day of
     * @return the first day of the given month in the given year
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {

        return ZonedDateTime.now(UTC).withYear(year).withMonth(month).with(firstDayOfMonth()).toLocalDate();
    }

    /**
     * Get the last day of the given month in the given year.
     *
     * @param year  of the month to get the last day of
     * @param month to get the first day of
     * @return the last day of the given month in the given year
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {

        return ZonedDateTime.now(UTC).withYear(year).withMonth(month).with(lastDayOfMonth()).toLocalDate();
    }
}
