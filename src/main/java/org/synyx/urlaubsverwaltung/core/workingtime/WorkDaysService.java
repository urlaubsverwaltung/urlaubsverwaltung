package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateFormat;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Service for calendar purpose.
 */
@Service
public class WorkDaysService {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;
    private final SettingsService settingsService;

    @Autowired
    public WorkDaysService(PublicHolidaysService publicHolidaysService, WorkingTimeService workingTimeService,
        SettingsService settingsService) {

        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
        this.settingsService = settingsService;
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

        Optional<WorkingTime> optionalWorkingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
                person, startDate);

        if (!optionalWorkingTime.isPresent()) {
            throw new NoValidWorkingTimeException("No working time found for User '" + person.getLoginName()
                + "' in period " + startDate.toString(DateFormat.PATTERN) + " - "
                + endDate.toString(DateFormat.PATTERN));
        }

        WorkingTime workingTime = optionalWorkingTime.get();

        FederalState federalState = getFederalState(workingTime);

        BigDecimal vacationDays = BigDecimal.ZERO;

        DateMidnight day = startDate;

        while (!day.isAfter(endDate)) {
            // value may be 1 for public holiday, 0 for not public holiday or 0.5 for Christmas Eve or New Year's Eve
            BigDecimal duration = publicHolidaysService.getWorkingDurationOfDate(day, federalState);

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


    private FederalState getFederalState(WorkingTime workingTime) {

        if (workingTime.getFederalStateOverride().isPresent()) {
            return workingTime.getFederalStateOverride().get();
        }

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
