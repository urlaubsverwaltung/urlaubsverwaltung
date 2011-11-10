/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.util;

import org.joda.time.DateMidnight;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author  aljona
 */
public class DateService {

    private static final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    public Integer countDaysBetweenTwoDates(DateMidnight startDate, DateMidnight endDate) {

        Date start = startDate.toDate();
        Date end = endDate.toDate();

        Calendar startCal = GregorianCalendar.getInstance();
        startCal.setTime(start);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        long startTime = startCal.getTimeInMillis();

        Calendar endCal = GregorianCalendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);

        long endTime = endCal.getTimeInMillis();

        return (int) (endTime - startTime) / MILLISECONDS_IN_DAY;
    }


    /**
     * get the current year (int)
     *
     * @return
     */
    public Integer getYear() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        Integer year = cal.get(Calendar.YEAR);

        return year;
    }


    /**
     * get the current full date as String
     *
     * @return
     */
    public String getDate() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        return day + "." + month + "." + year;
    }


    /**
     * get current day of month (int)
     *
     * @return
     */
    public int getDay() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.DAY_OF_MONTH);
    }


    /**
     * get current month (int)
     *
     * @return
     */
    public int getMonth() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.MONTH);
    }


    /**
     * get current day of week (Wochentag, int)
     *
     * @return
     */
    public int getDayOfWeek() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.DAY_OF_WEEK);
    }


    /**
     * get current hour of day (0-24, int)
     *
     * @return
     */
    public int getTime() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return cal.get(Calendar.HOUR_OF_DAY);
    }
}
