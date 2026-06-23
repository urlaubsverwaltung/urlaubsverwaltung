package org.synyx.urlaubsverwaltung.workingtime;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.UNNECESSARY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

@Service
public class WorkDaysCountService {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    public WorkDaysCountService(
        PublicHolidaysService publicHolidaysService,
        WorkingTimeService workingTimeService
    ) {
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
     * @param endDate   last day of the period to calculate the working days, must be after <code>startDate</code>
     * @param person    to calculate workdays in a certain time period
     * @return number of workdays in a certain time period
     * @throws IllegalArgumentException when <code>endDate</code> is before <code>startDate</code>
     */
    public BigDecimal getWorkDaysCount(DayLength dayLength, LocalDate startDate, LocalDate endDate, Person person) {

        final DateRange dateRange = new DateRange(startDate, endDate);

        final Map<DateRange, WorkingTime> workingTimes = workingTimeService.getWorkingTimesByPersonAndDateRange(person, dateRange);
        if (workingTimes.isEmpty()) {
            throw new WorkDaysCountException("No working times found for user '" + person.getId()
                + "' in period " + startDate.format(ofPattern(DD_MM_YYYY)) + " - " + endDate.format(ofPattern(DD_MM_YYYY)));
        }

        // Build a map from date to WorkingTime for quick lookup
        final Map<LocalDate, WorkingTime> workingTimesByDate = getLocalDateWorkingTime(workingTimes);
        // Fetch all public holidays for the entire period at once, grouped by federal state
        final Map<FederalState, List<PublicHoliday>> publicHolidaysByFederalState = getFederalStateListMap(workingTimes);

        BigDecimal vacationDays = BigDecimal.ZERO;
        LocalDate day = startDate;
        while (!day.isAfter(endDate)) {
            final WorkingTime workingTime = workingTimesByDate.get(day);
            if (workingTime == null) {
                day = day.plusDays(1);
                continue;
            }

            // Check if this day is a public holiday
            final List<PublicHoliday> publicHolidays = publicHolidaysByFederalState.get(workingTime.getFederalState());
            final LocalDate finalDay = day;
            final BigDecimal duration = publicHolidays.stream()
                .filter(holiday -> holiday.date().isEqual(finalDay))
                .map(PublicHoliday::getWorkingDuration)
                .findFirst()
                .orElse(BigDecimal.ONE);

            final BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(day.getDayOfWeek()).getDuration();
            vacationDays = vacationDays.add(duration.multiply(workingDuration));

            day = day.plusDays(1);
        }

        // vacation days < 1 day --> must not be divided, else an ArithmeticException is thrown
        if (vacationDays.compareTo(BigDecimal.ONE) < 0) {
            return vacationDays.setScale(1, UNNECESSARY);
        }

        return vacationDays.multiply(dayLength.getDuration()).setScale(1, UNNECESSARY);
    }

    private static @NonNull Map<LocalDate, WorkingTime> getLocalDateWorkingTime(Map<DateRange, WorkingTime> workingTimes) {
        final Map<LocalDate, WorkingTime> workingTimesByDate = new HashMap<>();
        workingTimes.forEach((key, value) -> key.iterator().forEachRemaining(localDate -> workingTimesByDate.put(localDate, value)));
        return workingTimesByDate;
    }

    private @NonNull Map<FederalState, List<PublicHoliday>> getFederalStateListMap(Map<DateRange, WorkingTime> workingTimes) {
        final Map<FederalState, List<PublicHoliday>> publicHolidaysByFederalState = new EnumMap<>(FederalState.class);
        for (Map.Entry<DateRange, WorkingTime> entry : workingTimes.entrySet()) {
            final DateRange dateRange = entry.getKey();
            final WorkingTime workingTime = entry.getValue();
            publicHolidaysByFederalState.computeIfAbsent(workingTime.getFederalState(), federalState -> publicHolidaysService.getPublicHolidays(dateRange.startDate(), dateRange.endDate(), federalState));
        }
        return publicHolidaysByFederalState;
    }
}
