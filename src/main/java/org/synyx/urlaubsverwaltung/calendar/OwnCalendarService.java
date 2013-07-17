
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
     * <p>This method calculates how many workdays are between declared start date and end date (official holidays are
     * ignored here)</p>
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  number of workdays
     */
    public double getWorkDays(DateMidnight startDate, DateMidnight endDate) {

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
     * This method calculates how many vacation days are used in the stated period (from start date to end date)
     * getWorkDays calculates the number of workdays, getPublicHolidays calculates the number of official holidays
     * within the workdays. Number of vacation days results from workdays minus official holidays.
     *
     * @param  dayLength
     * @param  startDate
     * @param  endDate
     *
     * @return  number of vacation days
     */
    public BigDecimal getVacationDays(DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        double vacDays = getWorkDays(startDate, endDate);

        vacDays = vacDays - jollydayCalendar.getPublicHolidays(startDate, endDate);

        return BigDecimal.valueOf(vacDays).multiply(dayLength.getDayLengthNumber());
    }
}
