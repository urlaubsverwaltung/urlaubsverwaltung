package org.synyx.urlaubsverwaltung.core.util;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;


/**
 * @author  Aljona Murygina
 */
public class DateUtil {

    private static final int FIRST_DAY_OF_MONTH = 1;
    private static final int LAST_DAY_OF_MONTH = 31;

    private static final int CHRISTMAS_EVE = 24;

    private static final int SATURDAY = DateTimeConstants.SATURDAY;
    private static final int SUNDAY = DateTimeConstants.SUNDAY;

    private DateUtil() {

        // Hide constructor for util classes
    }

    /**
     * checks if the given date is a work day.
     *
     * @param  date
     *
     * @return
     */
    public static boolean isWorkDay(DateMidnight date) {

        return !(date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY);
    }


    /**
     * checks if given date is before April.
     *
     * @param  date
     *
     * @return
     */
    public static boolean isBeforeApril(DateMidnight date) {

        return (date.getMonthOfYear() < DateTimeConstants.APRIL);
    }


    /**
     * this method checks if a given date is on Christmas Eve or on New Year's Eve.
     *
     * @param  date
     *
     * @return  true if date is on Christmas Eve or on New Year's Eve, else false
     */
    public static boolean isChristmasEveOrNewYearsEve(DateMidnight date) {

        boolean isInDecember = date.getMonthOfYear() == DateTimeConstants.DECEMBER;
        boolean isOn24thOrOn31st = date.getDayOfMonth() == CHRISTMAS_EVE || date.getDayOfMonth() == LAST_DAY_OF_MONTH;

        return isInDecember && isOn24thOrOn31st;
    }


    public static DateMidnight getFirstDayOfYear(int year) {

        return new DateMidnight(year, DateTimeConstants.JANUARY, FIRST_DAY_OF_MONTH);
    }


    public static DateMidnight getLastDayOfYear(int year) {

        return new DateMidnight(year, DateTimeConstants.DECEMBER, LAST_DAY_OF_MONTH);
    }


    public static DateMidnight getFirstDayOfMonth(int year, int month) {

        DateMidnight monthOfYear = DateMidnight.now().withYear(year).withMonthOfYear(month);

        return monthOfYear.dayOfMonth().withMinimumValue();
    }


    public static DateMidnight getLastDayOfMonth(int year, int month) {

        DateMidnight monthOfYear = DateMidnight.now().withYear(year).withMonthOfYear(month);

        return monthOfYear.dayOfMonth().withMaximumValue();
    }
}
