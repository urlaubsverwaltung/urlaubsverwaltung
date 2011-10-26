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
}
