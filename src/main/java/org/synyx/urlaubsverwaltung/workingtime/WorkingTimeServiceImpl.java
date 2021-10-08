package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHoliday;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

@Service
@Transactional
class WorkingTimeServiceImpl implements WorkingTimeService, WorkingTimeWriteService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeProperties workingTimeProperties;
    private final WorkingTimeRepository workingTimeRepository;
    private final PublicHolidaysService publicHolidaysService;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public WorkingTimeServiceImpl(WorkingTimeProperties workingTimeProperties, WorkingTimeRepository workingTimeRepository,
                                  PublicHolidaysService publicHolidaysService, SettingsService settingsService, Clock clock) {
        this.workingTimeProperties = workingTimeProperties;
        this.workingTimeRepository = workingTimeRepository;
        this.publicHolidaysService = publicHolidaysService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Override
    public void touch(List<Integer> workingDays, LocalDate validFrom, Person person) {
        touch(workingDays, validFrom, person, null);
    }

    @Override
    public void touch(List<Integer> workingDays, LocalDate validFrom, Person person, FederalState federalState) {

        WorkingTimeEntity workingTimeEntity = workingTimeRepository.findByPersonAndValidityDate(person, validFrom);

        /*
         * create a new WorkingTime object if no one existent for the given person and date
         */
        if (workingTimeEntity == null) {
            workingTimeEntity = new WorkingTimeEntity();
            workingTimeEntity.setPerson(person);
            workingTimeEntity.setValidFrom(validFrom);
        }

        resetWorkDays(workingTimeEntity);

        for (Integer workingDay : workingDays) {
            setWorkDay(workingTimeEntity, DayOfWeek.of(workingDay), DayLength.FULL);
        }

        workingTimeEntity.setFederalStateOverride(federalState);

        workingTimeRepository.save(workingTimeEntity);
        LOG.info("Created working time {} for person {}", workingTimeEntity, person);
    }

    @Override
    public Optional<WorkingTime> getWorkingTime(Person person, LocalDate date) {
        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);
        return Optional.ofNullable(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(person, date))
            .map(entity -> toWorkingTime(entity, federalStateCachedSupplier));
    }

    @Override
    public List<WorkingTime> getByPerson(Person person) {
        return toWorkingTimes(workingTimeRepository.findByPersonOrderByValidFromDesc(person));
    }

    @Override
    public List<WorkingTime> getByPersons(List<Person> persons) {
        return toWorkingTimes(workingTimeRepository.findByPersonIn(persons));
    }

    @Override
    public Map<DateRange, WorkingTime> getWorkingTimesByPersonAndDateRange(Person person, DateRange dateRange) {

        final List<WorkingTime> workingTimesByPerson = toWorkingTimes(workingTimeRepository.findByPersonOrderByValidFromDesc(person));
        final List<WorkingTime> workingTimeList = workingTimesByPerson.stream()
            .filter(workingTime -> !workingTime.getValidFrom().isAfter(dateRange.getEndDate()))
            .collect(toList());

        final HashMap<DateRange, WorkingTime> workingTimesOfPersonByDateRange = new HashMap<>();
        LocalDate nextEnd = dateRange.getEndDate();

        for (WorkingTime workingTime : workingTimeList) {

            final DateRange range;
            if (workingTime.getValidFrom().isBefore(dateRange.getStartDate())) {
                range = new DateRange(dateRange.getStartDate(), nextEnd);
            } else {
                range = new DateRange(workingTime.getValidFrom(), nextEnd);
            }

            workingTimesOfPersonByDateRange.put(range, workingTime);

            if (!workingTime.getValidFrom().isAfter(dateRange.getStartDate())) {
                return workingTimesOfPersonByDateRange;
            }

            nextEnd = workingTime.getValidFrom().minusDays(1);
        }

        return workingTimesOfPersonByDateRange;
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

        final LocalDate start = dateRange.getStartDate();
        final LocalDate end = dateRange.getEndDate();

        return persons.stream().map(person -> {
            final List<WorkingTime> workingTimesInDateRange = workingTimesByPerson.getOrDefault(person, List.of())
                .stream()
                .filter(workingTime -> !workingTime.getValidFrom().isAfter(end))
                .collect(toList());

            final Map<LocalDate, DayLength> dayLengthByDate = new HashMap<>();

            LocalDate nextEnd = end;

            for (WorkingTime workingTime : workingTimesInDateRange) {
                final FederalState federalState = workingTime.getFederalState();

                final DateRange workingTimeDateRange;
                if (workingTime.getValidFrom().isBefore(start)) {
                    workingTimeDateRange = new DateRange(start, nextEnd);
                } else {
                    workingTimeDateRange = new DateRange(workingTime.getValidFrom(), nextEnd);
                }

                for (LocalDate date : workingTimeDateRange) {
                    DayLength dayLengthForWeekDay = workingTime.getDayLengthForWeekDay(date.getDayOfWeek());
                    if (dayLengthForWeekDay.getDuration().signum() > 0) {
                        final Optional<PublicHoliday> maybePublicHoliday = publicHolidaysService.getPublicHoliday(date, federalState, workingTimeSettings);

                        if (maybePublicHoliday.isPresent()) {
                            final PublicHoliday publicHoliday = maybePublicHoliday.get();
                            if (dayLengthForWeekDay.equals(DayLength.FULL)) {
                                dayLengthForWeekDay = publicHoliday.getDayLength().getInverse();
                            } else {
                                if (dayLengthForWeekDay.equals(DayLength.MORNING)) {
                                    if (publicHoliday.isFull() || publicHoliday.isMorning()) {
                                        dayLengthForWeekDay = DayLength.ZERO;
                                    }
                                } else {
                                    if (publicHoliday.isFull() || publicHoliday.isNoon()) {
                                        dayLengthForWeekDay = DayLength.ZERO;
                                    }
                                }
                            }
                        }

                    }
                    dayLengthByDate.put(date, dayLengthForWeekDay);
                }

                if (workingTimeDateRange.getStartDate().equals(start)) {
                    break;
                }

                nextEnd = workingTime.getValidFrom().minusDays(1);
            }

            return Map.entry(person, new WorkingTimeCalendar(dayLengthByDate));
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<DateRange, FederalState> getFederalStatesByPersonAndDateRange(Person person, DateRange dateRange) {
        return getWorkingTimesByPersonAndDateRange(person, dateRange).entrySet().stream()
            .collect(toMap(Map.Entry::getKey, dateRangeWorkingTimeEntry -> dateRangeWorkingTimeEntry.getValue().getFederalState()));
    }

    @Override
    public FederalState getFederalStateForPerson(Person person, LocalDate date) {
        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);

        return getWorkingTime(person, date)
            .map(WorkingTime::getFederalState)
            .orElseGet(() -> {
                LOG.debug("No working time found for user '{}' equals or minor {}, using system federal state as fallback",
                    person.getId(), date.format(ofPattern(DD_MM_YYYY)));

                return federalStateCachedSupplier.get();
            });
    }

    @Override
    public FederalState getSystemDefaultFederalState() {
        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }

    @Override
    public void createDefaultWorkingTime(Person person) {
        final List<Integer> defaultWorkingDays;

        if (workingTimeProperties.isDefaultWorkingDaysDeactivated()) {
            defaultWorkingDays = settingsService.getSettings().getWorkingTimeSettings().getWorkingDays();
        } else {
            defaultWorkingDays = workingTimeProperties.getDefaultWorkingDays();
        }

        final LocalDate today = LocalDate.now(clock);
        this.touch(defaultWorkingDays, today.with(firstDayOfYear()), person);
    }

    @Override
    public void deleteAllByPerson(Person person) {
        workingTimeRepository.deleteByPerson(person);
    }

    private List<WorkingTime> toWorkingTimes(List<WorkingTimeEntity> workingTimeEntities) {
        final CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);
        return workingTimeEntities.stream()
            .map(workingTime -> toWorkingTime(workingTime, federalStateCachedSupplier))
            .collect(toList());
    }

    private static void resetWorkDays(WorkingTimeEntity workingTimeEntity) {
        workingTimeEntity.setMonday(DayLength.ZERO);
        workingTimeEntity.setTuesday(DayLength.ZERO);
        workingTimeEntity.setWednesday(DayLength.ZERO);
        workingTimeEntity.setThursday(DayLength.ZERO);
        workingTimeEntity.setFriday(DayLength.ZERO);
        workingTimeEntity.setSaturday(DayLength.ZERO);
        workingTimeEntity.setSunday(DayLength.ZERO);
    }

    private static void setWorkDay(WorkingTimeEntity workingTimeEntity, DayOfWeek dayOfWeek, DayLength dayLength) {
        switch (dayOfWeek) {
            case MONDAY:
                workingTimeEntity.setMonday(dayLength);
                break;
            case TUESDAY:
                workingTimeEntity.setTuesday(dayLength);
                break;
            case WEDNESDAY:
                workingTimeEntity.setWednesday(dayLength);
                break;
            case THURSDAY:
                workingTimeEntity.setThursday(dayLength);
                break;
            case FRIDAY:
                workingTimeEntity.setFriday(dayLength);
                break;
            case SATURDAY:
                workingTimeEntity.setSaturday(dayLength);
                break;
            case SUNDAY:
                workingTimeEntity.setSunday(dayLength);
                break;
        }
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

    private static class CachedSupplier<T> implements Supplier<T> {
        private T cachedValue;
        private final Supplier<T> supplier;

        CachedSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (cachedValue == null) {
                cachedValue = supplier.get();
            }
            return cachedValue;
        }
    }
}
