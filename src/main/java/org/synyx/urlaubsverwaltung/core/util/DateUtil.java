package org.synyx.urlaubsverwaltung.core.util;

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
    private static final int SATURDAY = DateTimeConstants.SATURDAY;
    private static final int SUNDAY = DateTimeConstants.SUNDAY;

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

        if ((date.getDayOfMonth() == CHRISTMAS_EVE && date.getMonthOfYear() == DateTimeConstants.DECEMBER)
                || (date.getDayOfMonth() == LAST_DAY_OF_MONTH && date.getMonthOfYear() == DateTimeConstants.DECEMBER)) {
            return true;
        } else {
            return false;
        }
    }


    public static DateMidnight getFirstDayOfYear(int year) {

        return new DateMidnight(year, DateTimeConstants.JANUARY, 1);
    }


    public static DateMidnight getLastDayOfYear(int year) {

        return new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
    }
}
