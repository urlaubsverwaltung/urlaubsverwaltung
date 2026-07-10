package org.synyx.urlaubsverwaltung.workingtime;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.math.RoundingMode.UNNECESSARY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.groupingBy;
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

        final BigDecimal vacationDays = getVacationDaysByYear(startDate, endDate, person).values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return applyDayLength(vacationDays, dayLength);
    }

    /**
     * Calculates the workdays like {@link #getWorkDaysCount(DayLength, LocalDate, LocalDate, Person)}, but split
     * into the years the given period spans. Every year of the period is present in the result,
     * even when it contains no workdays.
     *
     * @param dayLength personal daily working time of the given person
     * @param startDate start day of the period to calculate the working days
     * @param endDate   last day of the period to calculate the working days, must be after <code>startDate</code>
     * @param person    to calculate workdays in a certain time period
     * @return number of workdays for each year of the period, sorted by year
     * @throws IllegalArgumentException when <code>endDate</code> is before <code>startDate</code>
     */
    public SortedMap<Integer, BigDecimal> getWorkDaysCountByYear(DayLength dayLength, LocalDate startDate, LocalDate endDate, Person person) {

        final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
        getVacationDaysByYear(startDate, endDate, person)
            .forEach((year, vacationDays) -> workDaysByYear.put(year, applyDayLength(vacationDays, dayLength)));

        return workDaysByYear;
    }

    private SortedMap<Integer, BigDecimal> getVacationDaysByYear(LocalDate startDate, LocalDate endDate, Person person) {

        final DateRange dateRange = new DateRange(startDate, endDate);
        final Map<DateRange, WorkingTime> workingTimes = workingTimeService.getWorkingTimesByPersonAndDateRange(person, dateRange);
        return vacationDaysByYear(startDate, endDate, person, workingTimes);
    }

    /**
     * Batch variant of {@link #getWorkDaysCount(DayLength, LocalDate, LocalDate, Person)} for many applications.
     * <p>
     * The working times of all involved persons are loaded with a single query (instead of one query per
     * application), avoiding an N+1 query when the workdays of a whole list of applications are needed. Each
     * application's workdays are calculated for the application's own date range.
     *
     * @param applications to calculate the workdays for
     * @return the number of workdays per application
     */
    public Map<Application, BigDecimal> getWorkDaysCountForApplications(Collection<Application> applications) {
        return getWorkDaysCountForApplications(applications, null);
    }

    /**
     * Batch variant of {@link #getWorkDaysCount(DayLength, LocalDate, LocalDate, Person)} for many applications, where
     * the calculated period of each application is limited to (clipped to) the given date range.
     * <p>
     * The working times of all involved persons are loaded with a single query (instead of one query per
     * application), avoiding an N+1 query.
     *
     * @param applications        to calculate the workdays for
     * @param limitedToDateRange  the date range each application's period is clipped to
     * @return the number of workdays per application within the given date range
     */
    public Map<Application, BigDecimal> getWorkDaysCountForApplications(Collection<Application> applications, DateRange limitedToDateRange) {

        final Map<Application, BigDecimal> workDaysCountByApplication = new LinkedHashMap<>();
        batchVacationDaysByYear(applications, limitedToDateRange).forEach((application, vacationDaysByYear) -> {
            final BigDecimal vacationDays = vacationDaysByYear.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            workDaysCountByApplication.put(application, applyDayLength(vacationDays, application.getDayLength()));
        });

        return workDaysCountByApplication;
    }

    /**
     * Batch variant of {@link #getWorkDaysCountByYear(DayLength, LocalDate, LocalDate, Person)} for many applications.
     * <p>
     * The working times of all involved persons are loaded with a single query (instead of one query per
     * application), avoiding an N+1 query when the workdays of a whole list of applications are needed.
     *
     * @param applications to calculate the workdays for
     * @return the number of workdays per application, split by year and sorted by year
     */
    public Map<Application, SortedMap<Integer, BigDecimal>> getWorkDaysCountByYearForApplications(Collection<Application> applications) {

        final Map<Application, SortedMap<Integer, BigDecimal>> workDaysByYearByApplication = new LinkedHashMap<>();
        batchVacationDaysByYear(applications, null).forEach((application, vacationDaysByYear) -> {
            final SortedMap<Integer, BigDecimal> workDaysByYear = new TreeMap<>();
            vacationDaysByYear.forEach((year, vacationDays) -> workDaysByYear.put(year, applyDayLength(vacationDays, application.getDayLength())));
            workDaysByYearByApplication.put(application, workDaysByYear);
        });

        return workDaysByYearByApplication;
    }

    private Map<Application, SortedMap<Integer, BigDecimal>> batchVacationDaysByYear(Collection<Application> applications, @Nullable DateRange limitedToDateRange) {

        if (applications.isEmpty()) {
            return Map.of();
        }

        final List<Person> persons = applications.stream().map(Application::getPerson).distinct().toList();
        final Map<Person, List<WorkingTime>> workingTimesByPerson = workingTimeService.getByPersons(persons).stream()
            .collect(groupingBy(WorkingTime::getPerson));

        final Map<Application, SortedMap<Integer, BigDecimal>> vacationDaysByYearByApplication = new LinkedHashMap<>();
        for (Application application : applications) {
            final LocalDate startDate = clipStart(application.getStartDate(), limitedToDateRange);
            final LocalDate endDate = clipEnd(application.getEndDate(), limitedToDateRange);
            final List<WorkingTime> personWorkingTimes = workingTimesByPerson.getOrDefault(application.getPerson(), List.of());
            final Map<DateRange, WorkingTime> workingTimes =
                WorkingTimeServiceImpl.workingTimesByDateRange(personWorkingTimes, new DateRange(startDate, endDate));
            vacationDaysByYearByApplication.put(application,
                vacationDaysByYear(startDate, endDate, application.getPerson(), workingTimes));
        }

        return vacationDaysByYearByApplication;
    }

    private static LocalDate clipStart(LocalDate startDate, @Nullable DateRange limit) {
        if (limit == null || !limit.startDate().isAfter(startDate)) {
            return startDate;
        }
        return limit.startDate();
    }

    private static LocalDate clipEnd(LocalDate endDate, @Nullable DateRange limit) {
        if (limit == null || !limit.endDate().isBefore(endDate)) {
            return endDate;
        }
        return limit.endDate();
    }

    private SortedMap<Integer, BigDecimal> vacationDaysByYear(LocalDate startDate, LocalDate endDate, Person person, Map<DateRange, WorkingTime> workingTimes) {

        if (workingTimes.isEmpty()) {
            throw new WorkDaysCountException("No working times found for user '" + person.getId()
                + "' in period " + startDate.format(ofPattern(DD_MM_YYYY)) + " - " + endDate.format(ofPattern(DD_MM_YYYY)));
        }

        // Build a map from date to WorkingTime for quick lookup
        final Map<LocalDate, WorkingTime> workingTimesByDate = getLocalDateWorkingTime(workingTimes);
        final Map<LocalDate, List<PublicHoliday>> publicHolidaysByDate = getPublicHolidaysByDate(workingTimes);

        final SortedMap<Integer, BigDecimal> vacationDaysByYear = new TreeMap<>();
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            vacationDaysByYear.put(year, BigDecimal.ZERO);
        }

        LocalDate day = startDate;
        while (!day.isAfter(endDate)) {
            final WorkingTime workingTime = workingTimesByDate.get(day);
            if (workingTime == null) {
                day = day.plusDays(1);
                continue;
            }

            final List<PublicHoliday> publicHolidays = publicHolidaysByDate.getOrDefault(day, List.of());
            final BigDecimal duration = publicHolidays.stream()
                .map(PublicHoliday::getWorkingDuration)
                .findFirst()
                .orElse(BigDecimal.ONE);

            final BigDecimal workingDuration = workingTime.getDayLengthForWeekDay(day.getDayOfWeek()).getDuration();
            vacationDaysByYear.merge(day.getYear(), duration.multiply(workingDuration), BigDecimal::add);

            day = day.plusDays(1);
        }

        return vacationDaysByYear;
    }

    private static BigDecimal applyDayLength(BigDecimal vacationDays, DayLength dayLength) {

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

    private @NonNull Map<LocalDate, List<PublicHoliday>> getPublicHolidaysByDate(Map<DateRange, WorkingTime> workingTimes) {
        return workingTimes.entrySet().stream()
            .flatMap(entry -> publicHolidaysService.getPublicHolidays(
                    entry.getKey().startDate(),
                    entry.getKey().endDate(),
                    entry.getValue().getFederalState()
                ).stream()).collect(groupingBy(PublicHoliday::date));
    }
}
