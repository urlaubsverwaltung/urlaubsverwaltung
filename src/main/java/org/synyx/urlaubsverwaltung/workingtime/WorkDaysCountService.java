package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.math.RoundingMode.UNNECESSARY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

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

        final DateRange dateRange = new DateRange(startDate, endDate);

        final Map<DateRange, WorkingTime> workingTimes = workingTimeService.getWorkingTimesByPersonAndDateRange(person, dateRange);
        if (workingTimes.isEmpty()) {
            throw new WorkDaysCountException("No working times found for user '" + person.getId()
                + "' in period " + startDate.format(ofPattern(DD_MM_YYYY)) + " - " + endDate.format(ofPattern(DD_MM_YYYY)));
        }

        final Map<LocalDate, WorkingTime> workingTimesByDate = toLocalDateWorkingTime(workingTimes);

        BigDecimal vacationDays = BigDecimal.ZERO;
        LocalDate day = startDate;
        while (!day.isAfter(endDate)) {

            final WorkingTime workingTime = workingTimesByDate.get(day);

            // value may be 1 for public holiday, 0 for not public holiday or 0.5 for Christmas Eve or New Year's Eve
            final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(day, workingTime.getFederalState());
            final BigDecimal duration = maybePublicHoliday.isPresent() ? maybePublicHoliday.get().getWorkingDuration() : BigDecimal.ONE;

            final BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(day.getDayOfWeek()).getDuration();

            final BigDecimal result = duration.multiply(workingDuration);

            vacationDays = vacationDays.add(result);

            day = day.plusDays(1);
        }

        // vacation days < 1 day --> must not be divided, else an ArithmeticException is thrown
        if (vacationDays.compareTo(BigDecimal.ONE) < 0) {
            return vacationDays.setScale(1, UNNECESSARY);
        }

        return vacationDays.multiply(dayLength.getDuration()).setScale(1, UNNECESSARY);
    }

    private Map<LocalDate, WorkingTime> toLocalDateWorkingTime(Map<DateRange, WorkingTime> workingTimes) {
        final Map<LocalDate, WorkingTime> localDateWorkingTimeMap = new HashMap<>();
        workingTimes.forEach((key, value) -> key.iterator().forEachRemaining(localDate -> localDateWorkingTimeMap.put(localDate, value)));
        return localDateWorkingTimeMap;
    }
}
