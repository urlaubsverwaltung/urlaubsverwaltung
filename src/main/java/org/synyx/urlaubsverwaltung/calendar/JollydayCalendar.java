/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calendar;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;

import org.joda.time.DateMidnight;
import org.joda.time.chrono.GregorianChronology;

import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.util.HashSet;
import java.util.Set;


/**
 * @author  Aljona Murygina
 */
class JollydayCalendar {

    private static final double HALF_DAY = 0.5;
    private static final double FULL_DAY = 1.0;

    private HolidayManager manager = HolidayManager.getInstance("synyx");

    /**
     * Calculates number of public holidays between two given dates. If a public holiday is on Saturday or on Sunday it
     * is not counted among public holidays. Only public holidays on weekdays (Monday to Friday) are counted here.
     *
     * @param  startDate
     * @param  endDate
     *
     * @return  number of public holidays between startDate and endDate
     */
    double getPublicHolidays(DateMidnight start, DateMidnight end) {

        DateMidnight startDate = start.withChronology(GregorianChronology.getInstance());
        DateMidnight endDate = end.withChronology(GregorianChronology.getInstance());

        double numberOfHolidays = 0.0;

        // get all public holidays of this year
        Set<Holiday> holidays = manager.getHolidays(startDate.getYear());

        if (startDate.getYear() != endDate.getYear()) {
            Set<Holiday> holidaysNextYear = manager.getHolidays(endDate.getYear());
            holidays.addAll(holidaysNextYear);
        }

        // check if there are public holidays that are on Saturday or Sunday these days are worthless for the following
        // calculation

        // since it is not possible to remove elements during an iteration, you have to create a Set<Holiday> that
        // contains all public holidays that are on weekdays

        Set<Holiday> holidaysOnWeekdays = getOnlyHolidaysOnWeekdays(holidays);

        // check if start date and end date are equal
        if (startDate.equals(endDate)) {
            numberOfHolidays = calculate(holidaysOnWeekdays, startDate);
        } else {
            DateMidnight date = startDate;

            // iteration while startdatum != enddatum
            // calculation for all dates between start date (inclusive start date) and end date (exclusive end date!)
            while (!date.isAfter(endDate)) {
                // check if current date is a public holiday
                numberOfHolidays += calculate(holidaysOnWeekdays, date);
                date = date.plusDays(1);
            }
        }

        return numberOfHolidays;
    }


    /**
     * a Set of a year's all public holidays is given, this method creates a Set of public holidays that only are on
     * weekdays
     *
     * @param  holidays  Set of all public holidays of a year
     *
     * @return  a Set of public holidays that only are on weekdays
     */
    private Set<Holiday> getOnlyHolidaysOnWeekdays(Set<Holiday> holidays) {

        Set<Holiday> holidaysOnWeekdays = new HashSet<Holiday>();

        for (Holiday holiday : holidays) {
            if (DateUtil.isWorkDay(holiday.getDate().toDateMidnight())) {
                holidaysOnWeekdays.add(holiday);
            }
        }

        return holidaysOnWeekdays;
    }


    /**
     * calculates if a given date is a full public holiday (1.0), a half public holiday (0.5) or none (0.0)
     *
     * @param  holidaysOnWeekdays
     * @param  date
     *
     * @return
     */
    private double calculate(Set<Holiday> holidaysOnWeekdays, DateMidnight date) {

        double publicHolidays = 0.0;

        for (Holiday holiday : holidaysOnWeekdays) {
            // check if given date is a public holiday
            if ((date.toLocalDate()).equals(holiday.getDate())) {
                // check if given date is Christmas Eve or New Year's Eve
                // because these ones are counted as 0.5 days
                if (DateUtil.isChristmasEveOrNewYearsEve(holiday.getDate().toDateMidnight())) {
                    publicHolidays += HALF_DAY;
                } else {
                    // else 1.0 days are counted
                    publicHolidays += FULL_DAY;
                }
            }
        }
        
        // after having compared the date with all holidays of Jollyday's holiday set
        // check if the given date is on Corpus Christi (Fronleichnam)
        // this 'hardcoded' quickfix is necessary because Jollyday is not able to recognize Corpus Christ
        if(DateUtil.isCorpusChristi(date)) {
            publicHolidays += FULL_DAY;
        }
        
        
        return publicHolidays;
    }
}
