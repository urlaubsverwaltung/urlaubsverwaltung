package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.CachedSupplier;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

@Service
@Transactional
class WorkingTimeServiceImpl implements WorkingTimeService, WorkingTimeWriteService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeRepository workingTimeRepository;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public WorkingTimeServiceImpl(WorkingTimeRepository workingTimeRepository,
                                  SettingsService settingsService, Clock clock) {
        this.workingTimeRepository = workingTimeRepository;
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
            .filter(workingTime -> !workingTime.getValidFrom().isAfter(dateRange.endDate()))
            .toList();

        final HashMap<DateRange, WorkingTime> workingTimesOfPersonByDateRange = new HashMap<>();
        LocalDate nextEnd = dateRange.endDate();

        for (WorkingTime workingTime : workingTimeList) {

            final DateRange range;
            if (workingTime.getValidFrom().isBefore(dateRange.startDate())) {
                range = new DateRange(dateRange.startDate(), nextEnd);
            } else {
                range = new DateRange(workingTime.getValidFrom(), nextEnd);
            }

            workingTimesOfPersonByDateRange.put(range, workingTime);

            if (!workingTime.getValidFrom().isAfter(dateRange.startDate())) {
                return workingTimesOfPersonByDateRange;
            }

            nextEnd = workingTime.getValidFrom().minusDays(1);
        }

        return workingTimesOfPersonByDateRange;
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
        final List<Integer> defaultWorkingDays = settingsService.getSettings().getWorkingTimeSettings().getWorkingDays();
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
            .toList();
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
            case MONDAY -> workingTimeEntity.setMonday(dayLength);
            case TUESDAY -> workingTimeEntity.setTuesday(dayLength);
            case WEDNESDAY -> workingTimeEntity.setWednesday(dayLength);
            case THURSDAY -> workingTimeEntity.setThursday(dayLength);
            case FRIDAY -> workingTimeEntity.setFriday(dayLength);
            case SATURDAY -> workingTimeEntity.setSaturday(dayLength);
            case SUNDAY -> workingTimeEntity.setSunday(dayLength);
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
}
