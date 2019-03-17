package org.synyx.urlaubsverwaltung.core.util;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

/**
 * @author Aljona Murygina
 */
public final class DateUtil {

    private static final int DAY_OF_NEW_YEARS_EVE = 31;
    private static final int DAY_OF_CHRISTMAS_EVE = 24;

    private DateUtil() {

        // Hide constructor for util classes
    }

    /**
     * Check if the given date is a work day.
     *
     * @param date
     *            to check
     *
     * @return {@code true} if the given date is a work day, else {@code false}
     */
    public static boolean isWorkDay(DateMidnight date) {

        return !(date.getDayOfWeek() == DateTimeConstants.SATURDAY || date.getDayOfWeek() == DateTimeConstants.SUNDAY);
    }

    /**
     * Check if given date is before April of the given year
     *
     * @param date
     *            to check
     * @param year
     *            the year whose April the date will be compared to
     *
     * @return {@code true} if the given date is before April of the given year, {@code false} if it is in or after that April
     */
    public static boolean isBeforeApril(DateMidnight date, int year) {

        return date.getMonthOfYear() < DateTimeConstants.APRIL || date.getYear() < year;
    }

    /**
     * Check if given date is on Christmas Eve.
     *
     * @param date
     *            to check
     *
     * @return {@code true} if the given date is on Christmas Eve, else {@code false}
     */
    public static boolean isChristmasEve(DateMidnight date) {

        return date.getDayOfMonth() == DAY_OF_CHRISTMAS_EVE && date.getMonthOfYear() == DateTimeConstants.DECEMBER;
    }

    /**
     * Check if given date is on New Year's Eve.
     *
     * @param date
     *            to check
     *
     * @return {@code true} if the given date is on New Year's Eve, else {@code false}
     */
    public static boolean isNewYearsEve(DateMidnight date) {

        return date.getDayOfMonth() == DAY_OF_NEW_YEARS_EVE && date.getMonthOfYear() == DateTimeConstants.DECEMBER;
    }

    /**
     * Get the first day of the given year.
     *
     * @param year
     *            to get the first day of
     *
     * @return the first day of the given year
     */
    public static DateMidnight getFirstDayOfYear(int year) {

        return getFirstDayOfMonth(year, DateTimeConstants.JANUARY);
    }

    /**
     * Get the last day of the given year.
     *
     * @param year
     *            to get the last day of
     *
     * @return the last day of the given year
     */
    public static DateMidnight getLastDayOfYear(int year) {

        return getLastDayOfMonth(year, DateTimeConstants.DECEMBER);
    }

    /**
     * Get the first day of the given month in the given year.
     *
     * @param year
     *            of the month to get the first day of
     * @param month
     *            to get the first day of
     *
     * @return the first day of the given month in the given year
     */
    public static DateMidnight getFirstDayOfMonth(int year, int month) {

        DateMidnight monthOfYear = DateMidnight.now().withYear(year).withMonthOfYear(month);

        return monthOfYear.dayOfMonth().withMinimumValue();
    }

    /**
     * Get the last day of the given month in the given year.
     *
     * @param year
     *            of the month to get the last day of
     * @param month
     *            to get the first day of
     *
     * @return the last day of the given month in the given year
     */
    public static DateMidnight getLastDayOfMonth(int year, int month) {

        DateMidnight monthOfYear = DateMidnight.now().withYear(year).withMonthOfYear(month);

        return monthOfYear.dayOfMonth().withMaximumValue();
    }

    /**
     * Get the German name of a month as text.
     * 
     * @param month
     *            month as number from 1 to 12
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
