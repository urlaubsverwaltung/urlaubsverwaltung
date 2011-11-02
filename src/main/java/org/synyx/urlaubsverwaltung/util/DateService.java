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

    public Integer getYear() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        Integer year = cal.get(Calendar.YEAR);

        return year;
    }


    public String getDate() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        return day + "." + month + "." + year;
    }


    public int getDay() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.DAY_OF_MONTH);
    }


    public int getMonth() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.MONTH);
    }


    public int getDayOfWeek() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return (int) cal.get(Calendar.DAY_OF_WEEK);
    }


    public int getTime() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // heute

        return cal.get(Calendar.HOUR_OF_DAY);
    }
}
