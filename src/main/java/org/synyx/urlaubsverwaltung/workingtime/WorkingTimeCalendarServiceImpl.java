package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.CachedSupplier;
import org.synyx.urlaubsverwaltung.absence.DateRange;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.NO_WORKDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.WORKDAY;

@Service
class WorkingTimeCalendarServiceImpl implements WorkingTimeCalendarService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeRepository workingTimeRepository;
    private final PublicHolidaysService publicHolidaysService;
    private final SettingsService settingsService;

    WorkingTimeCalendarServiceImpl(WorkingTimeRepository workingTimeRepository, PublicHolidaysService publicHolidaysService, SettingsService settingsService) {
        this.workingTimeRepository = workingTimeRepository;
        this.publicHolidaysService = publicHolidaysService;
        this.settingsService = settingsService;
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
                } catch(StackOverflowError e) {
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
        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);
        final WorkingTimeSettings workingTimeSettings = settingsService.getSettings().getWorkingTimeSettings();

        final Map<Person, List<WorkingTime>> workingTimesByPerson = workingTimeRepository.findByPersonIsInOrderByValidFromDesc(persons)
            .stream()
            .map(entity -> toWorkingTime(entity, federalStateCachedSupplier))
            .collect(groupingBy(WorkingTime::getPerson));

        final LocalDate start = dateRange.startDate();
        final LocalDate end = dateRange.endDate();

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

                for (LocalDate date : workingTimeDateRange) {
                    dayLengthByDate.put(date, getWorkDayLengthForWeekDay(date, workingTime, workingTimeSettings));
                }

                if (workingTimeDateRange.startDate().equals(start)) {
                    break;
                }

                nextEnd = workingTime.getValidFrom().minusDays(1);
            }

            return Map.entry(person, new WorkingTimeCalendar(dayLengthByDate));
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private WorkingDayInformation getWorkDayLengthForWeekDay(LocalDate date, WorkingTime workingTime, WorkingTimeSettings workingTimeSettings) {
        final FederalState federalState = workingTime.getFederalState();

        final DayLength configuredWorkingTimeForDayOfWeek = workingTime.getDayLengthForWeekDay(date.getDayOfWeek());

        DayLength morning = configuredWorkingTimeForDayOfWeek.isFull() || configuredWorkingTimeForDayOfWeek.isMorning() ? MORNING : ZERO;
        WorkingTimeCalendarEntryType morningType = morning.isMorning() ? WORKDAY : NO_WORKDAY;

        DayLength noon = configuredWorkingTimeForDayOfWeek.isFull() || configuredWorkingTimeForDayOfWeek.isNoon() ? DayLength.NOON : ZERO;
        WorkingTimeCalendarEntryType noonType = noon.isNoon() ? WORKDAY : NO_WORKDAY;

        if (configuredWorkingTimeForDayOfWeek.getDuration().signum() > 0) {
            final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(date, federalState, workingTimeSettings);

            if (maybePublicHoliday.isPresent()) {

                final PublicHoliday publicHoliday = maybePublicHoliday.get();

                if (configuredWorkingTimeForDayOfWeek.isFull()) {
                    if (publicHoliday.dayLength().isFull()) {
                        morning = ZERO;
                        morningType = PUBLIC_HOLIDAY;
                        noon = ZERO;
                        noonType = PUBLIC_HOLIDAY;
                    } else if (publicHoliday.dayLength().isMorning()) {
                        morning = ZERO;
                        morningType = PUBLIC_HOLIDAY;
                        noon = DayLength.NOON;
                        noonType = WORKDAY;
                    } else if (publicHoliday.dayLength().isNoon()) {
                        morning = MORNING;
                        morningType = WORKDAY;
                        noon = ZERO;
                        noonType = PUBLIC_HOLIDAY;
                    } else {
                        morning = MORNING;
                        morningType = WORKDAY;
                        noon = DayLength.NOON;
                        noonType = WORKDAY;
                    }
                } else {
                    if (configuredWorkingTimeForDayOfWeek.isMorning()) {

                        noon = ZERO;
                        noonType = NO_WORKDAY;

                        if (publicHoliday.isFull() || publicHoliday.isMorning()) {
                            morning = ZERO;
                            morningType = PUBLIC_HOLIDAY;
                        } else {
                            morning = MORNING;
                            morningType = WORKDAY;
                        }
                    } else {

                        morning = ZERO;
                        morningType = NO_WORKDAY;

                        if (publicHoliday.isFull() || publicHoliday.isNoon()) {
                            noon = ZERO;
                            noonType = PUBLIC_HOLIDAY;
                        } else {
                            noon = DayLength.NOON;
                            noonType = WORKDAY;
                        }
                    }
                }
            }
        }

        DayLength calculatedDayLength = configuredWorkingTimeForDayOfWeek;
        if (morning.isMorning() && noon.isNoon()) {
            calculatedDayLength = DayLength.FULL;
        } else if (morning == ZERO && noon == ZERO) {
            calculatedDayLength = ZERO;
        } else if (morning == ZERO && noon.isNoon()) {
            calculatedDayLength = DayLength.NOON;
        } else if (morning.isMorning() && noon == ZERO) {
            calculatedDayLength = MORNING;
        }

        return new WorkingDayInformation(calculatedDayLength, morningType, noonType);
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
        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
