package org.synyx.urlaubsverwaltung.dev;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Component;

import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.CalcUtil;

import java.math.BigDecimal;


/**
 * Helper class to check durations.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
@ConditionalOnProperty("testdata.create")
class DurationChecker {

    private final WorkDaysService workDaysService;

    @Autowired
    DurationChecker(WorkDaysService workDaysService) {

        this.workDaysService = workDaysService;
    }

    /**
     * Check if the given dates are in the current year.
     *
     * @param  start  to be checked if in the current year
     * @param  end  to be checked if in the current year
     *
     * @return  {@code true} if both dates are in the current year, else {@code false}
     */
    boolean startAndEndDatesAreInCurrentYear(DateMidnight start, DateMidnight end) {

        int currentYear = DateMidnight.now().getYear();

        return start.getYear() == currentYear && end.getYear() == currentYear;
    }


    /**
     * Check if the period between the given start and end date is greater than zero days, the custom working time of
     * the given person is concerned.
     *
     * @param  start  of the period
     * @param  end  of the period
     * @param  person  to use the working time for calculation
     *
     * @return  {@code true} if the period duration is greater than zero, else {@code false}
     */
    boolean durationIsGreaterThanZero(DateMidnight start, DateMidnight end, Person person) {

        BigDecimal workDays = workDaysService.getWorkDays(DayLength.FULL, start, end, person);

        return CalcUtil.isPositive(workDays);
    }
}
