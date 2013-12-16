
package org.synyx.urlaubsverwaltung.calendar;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;


/**
 * Service for calendar purpose.
 *
 * @author  Aljona Murygina
 */
public class OwnCalendarService {

    private JollydayCalendar jollydayCalendar;
    private WorkingTimeService workingTimeService;

    @Autowired
    public OwnCalendarService(JollydayCalendar jollydayCalendar, WorkingTimeService workingTimeService) {

        this.jollydayCalendar = jollydayCalendar;
        this.workingTimeService = workingTimeService;
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
     * Calculates the number of workdays considering the individual person's working time.
     *
     * @param  startDate
     * @param  endDate
     * @param  workingTime
     *
     * @return  number of workdays
     */
    protected BigDecimal getPersonalWorkDays(DateMidnight startDate, DateMidnight endDate, WorkingTime workingTime) {

        BigDecimal workDays = BigDecimal.ZERO;

        DateMidnight day = startDate;

        while (!day.isAfter(endDate)) {
            int dayOfWeek = day.getDayOfWeek();
            DayLength dayLength = workingTime.getDayLengthForWeekDay(dayOfWeek);
            workDays = workDays.add(dayLength.getDuration());

            day = day.plusDays(1);
        }

        return workDays;
    }


    /**
     * This method calculates how many workdays are used in the stated period (from start date to end date) considering
     * the personal working time of the given person, getNumberOfPublicHolidays calculates the number of official
     * holidays within the personal workdays period. Number of workdays results from difference between personal
     * workdays and official holidays.
     *
     * @param  dayLength
     * @param  startDate
     * @param  endDate
     * @param  person
     *
     * @return  number of workdays
     */
    public BigDecimal getWorkDays(DayLength dayLength, DateMidnight startDate, DateMidnight endDate, Person person) {

        WorkingTime workingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(person, startDate);

        BigDecimal vacationDays = BigDecimal.ZERO;

        DateMidnight day = startDate;

        while (!day.isAfter(endDate)) {
            // value may be 1 for public holiday, 0 for not public holiday or 0.5 for Christmas Eve or New Year's Eve
            BigDecimal duration = jollydayCalendar.getWorkingDurationOfDate(day);

            int dayOfWeek = day.getDayOfWeek();
            BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(dayOfWeek).getDuration();

            BigDecimal result = duration.multiply(workingDuration);

            vacationDays = vacationDays.add(result);

            day = day.plusDays(1);
        }

        // vacation days < 1 day --> must not be divided, else an ArithmeticException is thrown
        if (vacationDays.compareTo(BigDecimal.ONE) < 0) {
            return vacationDays.setScale(1);
        }

        return vacationDays.multiply(dayLength.getDuration()).setScale(1);
    }
}
