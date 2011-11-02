/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.util;

import java.util.Calendar;
import java.util.Date;


/**
 * @author  aljona
 */
public class DateService {

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
