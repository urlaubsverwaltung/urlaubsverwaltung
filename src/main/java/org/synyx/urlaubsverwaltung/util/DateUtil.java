package org.synyx.urlaubsverwaltung.util;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;


/**
 * @author  Aljona Murygina
 */
public class DateUtil {

    private static final Logger LOG = Logger.getLogger("errorLog");

    private static final int CHRISTMAS_EVE = 24;
    private static final int LAST_DAY_OF_MONTH = 31;
    private static final int FIRST_DAY_OF_MONTH = 1;
    private static final int SATURDAY = DateTimeConstants.SATURDAY;
    private static final int SUNDAY = DateTimeConstants.SUNDAY;

    private static final String PROP_FILE = "custom.properties";
    private static final String PROP_KEY = "holiday.corpus.christi";

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
     * checks if the given period is before April.
     *
     * @param  startMonth
     * @param  endMonth
     *
     * @return
     */
    public static boolean isBeforeApril(int startMonth, int endMonth) {

        return (DateTimeConstants.JANUARY <= startMonth && startMonth <= DateTimeConstants.MARCH)
            && (DateTimeConstants.JANUARY <= endMonth && endMonth <= DateTimeConstants.MARCH);
    }


    /**
     * checks if the given period is after April.
     *
     * @param  startMonth
     * @param  endMonth
     *
     * @return
     */
    public static boolean isAfterApril(int startMonth, int endMonth) {

        return (DateTimeConstants.APRIL <= startMonth && startMonth <= DateTimeConstants.DECEMBER)
            && (DateTimeConstants.APRIL <= endMonth && endMonth <= DateTimeConstants.DECEMBER);
    }


    /**
     * checks if the given period spans March and April.
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  true if startMonth is March and endMonth is April, false otherwise
     */
    public static boolean spansMarchAndApril(DateMidnight startDate, DateMidnight endDate) {

        return (isBeforeApril(startDate) && isAfterApril(endDate));
    }


    /**
     * checks if given period spans December and January.
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  true if startMonth is December and endMonth is January, false otherwise
     */
    public static boolean spansDecemberAndJanuary(DateMidnight startDate, DateMidnight endDate) {

        return (startDate.getYear() != endDate.getYear());
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
     * checks if given date is after April.
     *
     * @param  date
     *
     * @return
     */
    public static boolean isAfterApril(DateMidnight date) {

        return (date.getMonthOfYear() >= DateTimeConstants.APRIL);
    }


    /**
     * this method checks if a given date is on Christmas Eve or on New Year's Eve.
     *
     * @param  date
     *
     * @return  true if date is on Christmas Eve or on New Year's Eve, else false
     */
    public static boolean isChristmasEveOrNewYearsEve(DateMidnight date) {

        if ((date.getDayOfMonth() == CHRISTMAS_EVE && date.getMonthOfYear() == DateTimeConstants.DECEMBER)
                || (date.getDayOfMonth() == LAST_DAY_OF_MONTH && date.getMonthOfYear() == DateTimeConstants.DECEMBER)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * this method checks if a given date is on first January (returns true) or not (returns false).
     *
     * @param  date
     *
     * @return
     */
    public static boolean isFirstJanuary(DateMidnight date) {

        return ((date.getDayOfMonth() == FIRST_DAY_OF_MONTH) && (date.getMonthOfYear() == DateTimeConstants.JANUARY));
    }
}
