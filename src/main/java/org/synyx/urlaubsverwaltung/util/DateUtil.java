package org.synyx.urlaubsverwaltung.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class DateUtil {

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
     * Check if the given date is on a weekend.
     *
     * @param date to check
     * @return {@code true} if the given date is on a weekend, else {@code false}
     */
    public static boolean isWeekend(LocalDate date) {
        return !isWorkDay(date);
    }
}
