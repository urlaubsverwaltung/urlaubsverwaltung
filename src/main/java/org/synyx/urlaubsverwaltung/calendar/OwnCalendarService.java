/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.domain.Application;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class OwnCalendarService {

    private JollydayCalendar jollydayCalendar = new JollydayCalendar();

    /**
     * Note: the start date must be before or equal the end date; this is validated prior to that method
     *
     * <p>This method calculates how many workdays are between declared start date and end date (official holidays are
     * ignored here)</p>
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  number of workdays
     */
    public double getWorkDays(DateMidnight startDate, DateMidnight endDate) {

        double workDays = 1.0;

        if (!startDate.equals(endDate)) {
            DateMidnight day = startDate;

            while (!day.equals(endDate)) {
                if (!(day.getDayOfWeek() == DateTimeConstants.SATURDAY
                            || day.getDayOfWeek() == DateTimeConstants.SUNDAY)) {
                    workDays++;
                }

                day = day.plusDays(1);
            }
        }

        return workDays;
    }


    /**
     * This method calculates how many vacation days are used in the stated period (from start date to end date)
     * getWorkDays calculates the number of workdays, getPublicHolidays calculates the number of official holidays
     * within the workdays. Number of vacation days results from workdays minus official holidays.
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  number of vacation days
     */
    public BigDecimal getVacationDays(Application application, DateMidnight startDate, DateMidnight endDate) {

        double vacDays = getWorkDays(startDate, endDate);

        vacDays = vacDays - jollydayCalendar.getPublicHolidays(startDate, endDate);

        return BigDecimal.valueOf(vacDays).multiply(application.getHowLong().getDayLengthNumber());
    }
}
