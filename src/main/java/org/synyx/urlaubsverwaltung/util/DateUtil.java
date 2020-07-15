package org.synyx.urlaubsverwaltung.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoField;

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
    public static boolean isWorkDay(Instant date) {

        return !(DayOfWeek.from(date) == DayOfWeek.SATURDAY || DayOfWeek.from(date) == DayOfWeek.SUNDAY);
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
    public static boolean isChristmasEve(Instant date) {

        return date.get(ChronoField.DAY_OF_WEEK) == DAY_OF_CHRISTMAS_EVE && Month.from(date) == Month.DECEMBER;
    }

    /**
     * Check if given date is on New Year's Eve.
     *
     * @param date to check
     * @return {@code true} if the given date is on New Year's Eve, else {@code false}
     */
    public static boolean isNewYearsEve(Instant date) {

        return date.get(ChronoField.DAY_OF_MONTH) == DAY_OF_NEW_YEARS_EVE && Month.from(date) == Month.DECEMBER;
    }

    /**
     * Get the first day of the given year.
     *
     * @param year to get the first day of
     * @return the first day of the given year
     */
    public static Instant getFirstDayOfYear(int year) {

        return Instant.from(YearMonth.of(year, 1).atDay(1));
    }

    /**
     * Get the last day of the given year.
     *
     * @param year to get the last day of
     * @return the last day of the given year
     */
    public static Instant getLastDayOfYear(int year) {

        return Instant.from(YearMonth.of(year, 12).atEndOfMonth());
    }

    /**
     * Get the first day of the given month in the given year.
     *
     * @param year  of the month to get the first day of
     * @param month to get the first day of
     * @return the first day of the given month in the given year
     */
    public static Instant getFirstDayOfMonth(int year, int month) {

        return Instant.from(YearMonth.of(year, month).atDay(1));
    }

    /**
     * Get the last day of the given month in the given year.
     *
     * @param year  of the month to get the last day of
     * @param month to get the first day of
     * @return the last day of the given month in the given year
     */
    public static Instant getLastDayOfMonth(int year, int month) {

        return Instant.from(YearMonth.of(year, month).atEndOfMonth());
    }

    /**
     * Get the German name of a month as text.
     *
     * @param month month as number from 1 to 12
     * @return month as String
     */
    public static String getMonthName(Integer month) {

        String[] monthNameArray = new String[12];

        monthNameArray[0] = "Januar";
        monthNameArray[1] = "Februar";
        monthNameArray[2] = "März";
        monthNameArray[3] = "April";
        monthNameArray[4] = "Mai";
        monthNameArray[5] = "Juni";
        monthNameArray[6] = "Juli";
        monthNameArray[7] = "August";
        monthNameArray[8] = "September";
        monthNameArray[9] = "Oktober";
        monthNameArray[10] = "November";
        monthNameArray[11] = "Dezember";

        return monthNameArray[month - 1];
    }

}
