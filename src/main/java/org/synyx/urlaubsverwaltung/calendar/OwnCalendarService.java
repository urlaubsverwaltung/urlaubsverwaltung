
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;


/**
 * Service for calendar purpose.
 *
 * @author  Aljona Murygina
 */
public class OwnCalendarService {

    private JollydayCalendar jollydayCalendar;

    public OwnCalendarService(JollydayCalendar jollydayCalendar) {

        this.jollydayCalendar = jollydayCalendar;
    }

    /**
     * Note: the start date must be before or equal the end date; this is validated prior to that method
     *
     * <p>This method calculates how many weekdays are between declared start date and end date (official holidays are
     * ignored here)</p>
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  number of weekdays
     */
    public double getWeekDays(DateMidnight startDate, DateMidnight endDate) {

        double workDays = 0.0;

        if (!startDate.equals(endDate)) {
            DateMidnight day = startDate;

            while (!day.isAfter(endDate)) {
                if (DateUtil.isWorkDay(day)) {
                    workDays++;
                }

                day = day.plusDays(1);
            }
        } else {
            if (DateUtil.isWorkDay(startDate)) {
                workDays++;
            }
        }

        return workDays;
    }


    /**
     * This method calculates how many workdays are used in the stated period (from start date to end date) getWeekDays
     * calculates the number of weekdays, getPublicHolidays calculates the number of official holidays within the week
     * days period. Number of workdays results from difference between weekdays and official holidays.
     *
     * @param  dayLength
     * @param  startDate
     * @param  endDate
     *
     * @return  number of workdays
     */
    public BigDecimal getWorkDays(DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        double vacDays = getWeekDays(startDate, endDate);

        vacDays = vacDays - jollydayCalendar.getPublicHolidays(startDate, endDate);

        return BigDecimal.valueOf(vacDays).multiply(dayLength.getDayLengthNumber());
    }
}
