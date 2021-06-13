package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.math.RoundingMode.UNNECESSARY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.util.DateFormat.DD_MM_YYYY;


/**
 * Service for calendar purpose.
 */
@Service
public class WorkDaysCountService {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public WorkDaysCountService(PublicHolidaysService publicHolidaysService, WorkingTimeService workingTimeService) {
        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
    }

    /**
     * This method calculates how many weekdays are between declared start date and end date
     * (official holidays are ignored here)
     * <p>
     * Note: the start date must be before or equal the end date; this is validated prior to that method
     *
     * @param startDate the first day of the time period to calculate workdays
     * @param endDate   the last day of the time period to calculate workdays
     * @return number of weekdays
     */
    public double getWeekDaysCount(LocalDate startDate, LocalDate endDate) {

        double workDays = 0.0;

        if (!startDate.equals(endDate)) {
            LocalDate day = startDate;

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
     * @param dayLength personal daily working time of the given person
     * @param startDate start day of the period to calculate the working days
     * @param endDate   last day of the period to calculate the working days
     * @param person    to calculate workdays in a certain time period
     * @return number of workdays in a certain time period
     */
    public BigDecimal getWorkDaysCount(DayLength dayLength, LocalDate startDate, LocalDate endDate, Person person) {

        final Optional<WorkingTime> optionalWorkingTime = workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(
            person, startDate);

        if (optionalWorkingTime.isEmpty()) {
            throw new WorkDaysCountException("No working time found for User '" + person.getId()
                + "' in period " + startDate.format(ofPattern(DD_MM_YYYY)) + " - " + endDate.format(ofPattern(DD_MM_YYYY)));
        }

        final WorkingTime workingTime = optionalWorkingTime.get();
        final FederalState federalState = workingTime.getFederalState();

        BigDecimal vacationDays = BigDecimal.ZERO;
        LocalDate day = startDate;
        while (!day.isAfter(endDate)) {
            // value may be 1 for public holiday, 0 for not public holiday or 0.5 for Christmas Eve or New Year's Eve
            BigDecimal duration = publicHolidaysService.getWorkingDurationOfDate(day, federalState);

            BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(day.getDayOfWeek()).getDuration();

            BigDecimal result = duration.multiply(workingDuration);

            vacationDays = vacationDays.add(result);

            day = day.plusDays(1);
        }

        // vacation days < 1 day --> must not be divided, else an ArithmeticException is thrown
        if (vacationDays.compareTo(BigDecimal.ONE) < 0) {
            return vacationDays.setScale(1, UNNECESSARY);
        }

        return vacationDays.multiply(dayLength.getDuration()).setScale(1, UNNECESSARY);
    }
}
