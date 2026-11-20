package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.CachedSupplier;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosure;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.period.DateLimits.requireCalculableDuration;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.COMPANY_CLOSURE;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@Service
class WorkingTimeCalendarServiceImpl implements WorkingTimeCalendarService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeRepository workingTimeRepository;
    private final PublicHolidaysService publicHolidaysService;
    private final SettingsService settingsService;
    private final CompanyClosureService companyClosureService;

    WorkingTimeCalendarServiceImpl(WorkingTimeRepository workingTimeRepository, PublicHolidaysService publicHolidaysService, SettingsService settingsService, CompanyClosureService companyClosureService) {
        this.workingTimeRepository = workingTimeRepository;
        this.publicHolidaysService = publicHolidaysService;
        this.settingsService = settingsService;
        this.companyClosureService = companyClosureService;
    }

    @Override
    public Optional<LocalDate> getNextWorkingDayFollowingTo(Person person, LocalDate date) {

        LOG.info("calculate next working day following to date={} of person={}", date, person.getId());

        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);

        final List<WorkingTime> workingTimes = workingTimeRepository.findByPersonIsInOrderByValidFromDesc(List.of(person))
            .stream()
            .map(entity -> toWorkingTime(entity, federalStateCachedSupplier))
            .toList();

        if (workingTimes.isEmpty()) {
            LOG.warn("expected workingTimes to exist for person={} but got none.", person.getId());
            return Optional.empty();
        }

        final Optional<LocalDate> nextWorkingDay = getSameOrNextWorkingDay(
            date.plusDays(1), d -> getWorkingTimeForDate(d, workingTimes));

        if (nextWorkingDay.isEmpty()) {
            LOG.warn("could not find next workingDay of person={} with date={}", person.getId(), date);
        } else {
            LOG.debug("found next workingDay of person={} with date={}", person.getId(), date);
        }

        return nextWorkingDay;
    }

    private Optional<WorkingTime> getWorkingTimeForDate(LocalDate date, List<WorkingTime> sortedWorkingTimes) {
        // workingTimes are ordered by validFrom last to first (2024-07-01, 2023-07-01, ...)
        // the first validFrom before searched date is the one to extract the info from
        for (WorkingTime workingTime : sortedWorkingTimes) {
            final LocalDate validFrom = workingTime.getValidFrom();
            if (validFrom.isEqual(date) || validFrom.isBefore(date)) {
                return Optional.of(workingTime);
            }
        }
        return Optional.empty();
    }

    private Optional<LocalDate> getSameOrNextWorkingDay(LocalDate date, Function<LocalDate, Optional<WorkingTime>> workingTimeSupplier) {
        return workingTimeSupplier.apply(date).flatMap(workingTime -> {
            if (workingTime.isWorkingDay(date.getDayOfWeek())) {
                return Optional.of(date);
            } else {
                try {
                    return getSameOrNextWorkingDay(date.plusDays(1), workingTimeSupplier);
                } catch (StackOverflowError _) {
                    LOG.warn("could not get same or next workingDay due to workingTime without working days.");
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, Year year) {
        return getWorkingTimesByPersons(persons, new DateRange(year.atDay(1), year.atDay(1).with(lastDayOfYear())));
    }

    @Override
    public Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, DateRange dateRange) {
        requireCalculableDuration(dateRange);

        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);

        final Map<Person, List<WorkingTime>> workingTimesByPerson = workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)
            .stream()
            .map(entity -> toWorkingTime(entity, federalStateCachedSupplier))
            .collect(groupingBy(WorkingTime::getPerson));

        final LocalDate start = dateRange.startDate();
        final LocalDate end = dateRange.endDate();

        // the public holidays of a federal state are the same for every person. fetch them once per federal state for
        // the whole date range, instead of once per person and day.
        final Map<FederalState, Map<LocalDate, PublicHoliday>> publicHolidaysByFederalState = new EnumMap<>(FederalState.class);

        // no calendar entry exists before the earliest working time. fetching public holidays before it is wasted work,
        // and for a start far in the past it would mean computing every single year in between.
        final LocalDate publicHolidaysStart = workingTimesByPerson.values().stream()
            .flatMap(List::stream)
            .map(WorkingTime::getValidFrom)
            .filter(validFrom -> !validFrom.isAfter(end))
            .min(naturalOrder())
            .filter(validFrom -> validFrom.isAfter(start))
            .orElse(start);

        final Map<LocalDate, DayLength> companyClosuresByDate = companyClosureService.getCompanyClosures(publicHolidaysStart, end).stream()
            .collect(toMap(CompanyClosure::date, CompanyClosure::dayLength));

        return persons.stream().map(person -> {

            final List<WorkingTime> workingTimesInDateRange = workingTimesByPerson.getOrDefault(person, List.of())
                .stream()
                .filter(workingTime -> !workingTime.getValidFrom().isAfter(end))
                .toList();

            final Map<LocalDate, WorkingDayInformation> dayLengthByDate = new HashMap<>();

            LocalDate nextEnd = end;

            for (WorkingTime workingTime : workingTimesInDateRange) {

                final DateRange workingTimeDateRange;
                if (workingTime.getValidFrom().isBefore(start)) {
                    workingTimeDateRange = new DateRange(start, nextEnd);
                } else {
                    workingTimeDateRange = new DateRange(workingTime.getValidFrom(), nextEnd);
                }

                final Map<LocalDate, PublicHoliday> publicHolidaysByDate = publicHolidaysByFederalState.computeIfAbsent(
                    workingTime.getFederalState(), federalState -> getPublicHolidaysByDate(publicHolidaysStart, end, federalState));

                for (LocalDate date : workingTimeDateRange) {
                    dayLengthByDate.put(date, getWorkDayLengthForWeekDay(date, workingTime, publicHolidaysByDate, companyClosuresByDate));
                }

                if (workingTimeDateRange.startDate().equals(start)) {
                    break;
                }

                nextEnd = workingTime.getValidFrom().minusDays(1);
            }

            return Map.entry(person, new WorkingTimeCalendar(dayLengthByDate));
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<LocalDate, PublicHoliday> getPublicHolidaysByDate(LocalDate start, LocalDate end, FederalState federalState) {
        // two public holidays can fall on the same date. public holidays are always full days, so which one is kept
        // does not change the day length.
        return publicHolidaysService.getPublicHolidays(start, end, federalState).stream()
            .collect(toMap(PublicHoliday::date, identity(), (first, _) -> first));
    }

    private WorkingDayInformation getWorkDayLengthForWeekDay(LocalDate date, WorkingTime workingTime, Map<LocalDate, PublicHoliday> publicHolidaysByDate, Map<LocalDate, DayLength> companyClosuresByDate) {
        final DayLength configured = workingTime.getDayLengthForWeekDay(date.getDayOfWeek());
        final PublicHoliday publicHoliday = publicHolidaysByDate.get(date);
        final DayLength publicHolidayLength = publicHoliday == null ? ZERO : publicHoliday.dayLength();
        final DayLength closureLength = companyClosuresByDate.getOrDefault(date, ZERO);

        final WorkingTimeCalendarEntryType morningType = halfType(configured.isFull() || configured.isMorning(), covers(publicHolidayLength, MORNING), covers(closureLength, MORNING));
        final WorkingTimeCalendarEntryType noonType = halfType(configured.isFull() || configured.isNoon(), covers(publicHolidayLength, DayLength.NOON), covers(closureLength, DayLength.NOON));

        final boolean morningWork = morningType == WORKDAY;
        final boolean noonWork = noonType == WORKDAY;
        final DayLength dayLength;
        if (morningWork && noonWork) {
            dayLength = DayLength.FULL;
        } else if (morningWork) {
            dayLength = MORNING;
        } else if (noonWork) {
            dayLength = DayLength.NOON;
        } else {
            dayLength = ZERO;
        }

        return new WorkingDayInformation(dayLength, morningType, noonType);
    }

    private static boolean covers(DayLength closedOrHoliday, DayLength half) {
        return closedOrHoliday == DayLength.FULL || closedOrHoliday == half;
    }

    private static WorkingTimeCalendarEntryType halfType(boolean configuredWorkingHalf, boolean publicHoliday, boolean companyClosure) {
        if (!configuredWorkingHalf) {
            return NO_WORKDAY;
        }
        if (publicHoliday) {
            return PUBLIC_HOLIDAY;
        }
        return companyClosure ? COMPANY_CLOSURE : WORKDAY;
    }

    private static WorkingTime toWorkingTime(WorkingTimeEntity workingTimeEntity, Supplier<FederalState> defaultFederalStateProvider) {

        final boolean isDefaultFederalState = workingTimeEntity.getFederalStateOverride() == null;
        final FederalState federalState = isDefaultFederalState ? defaultFederalStateProvider.get() : workingTimeEntity.getFederalStateOverride();

        final WorkingTime workingTime = new WorkingTime(workingTimeEntity.getPerson(), workingTimeEntity.getValidFrom(), federalState, isDefaultFederalState);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            final DayLength dayLength = dayLengthForDayOfWeek(workingTimeEntity, dayOfWeek);
            workingTime.setDayLengthForWeekDay(dayOfWeek, dayLength);
        }

        return workingTime;
    }

    private static DayLength dayLengthForDayOfWeek(WorkingTimeEntity workingTimeEntity, DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> workingTimeEntity.getMonday();
            case TUESDAY -> workingTimeEntity.getTuesday();
            case WEDNESDAY -> workingTimeEntity.getWednesday();
            case THURSDAY -> workingTimeEntity.getThursday();
            case FRIDAY -> workingTimeEntity.getFriday();
            case SATURDAY -> workingTimeEntity.getSaturday();
            case SUNDAY -> workingTimeEntity.getSunday();
        };
    }

    private FederalState getSystemDefaultFederalState() {
        return settingsService.getSettings().getPublicHolidaysSettings().getFederalState();
    }
}
