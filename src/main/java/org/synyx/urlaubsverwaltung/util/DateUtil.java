package org.synyx.urlaubsverwaltung.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

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
}
