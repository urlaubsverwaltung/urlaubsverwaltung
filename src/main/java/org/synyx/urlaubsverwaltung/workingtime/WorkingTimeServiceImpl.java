package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateFormat.DD_MM_YYYY;

@Service
@Transactional
class WorkingTimeServiceImpl implements WorkingTimeService, WorkingTimeWriteService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeProperties workingTimeProperties;
    private final WorkingTimeRepository workingTimeRepository;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public WorkingTimeServiceImpl(WorkingTimeProperties workingTimeProperties, WorkingTimeRepository workingTimeRepository,
                                  SettingsService settingsService, Clock clock) {
        this.workingTimeProperties = workingTimeProperties;
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
    public List<WorkingTime> getByPerson(Person person) {
        return toWorkingTime(workingTimeRepository.findByPersonOrderByValidFromDesc(person));
    }

    @Override
    public List<WorkingTime> getByPersonsAndDateInterval(List<Person> persons, LocalDate start, LocalDate end) {
        return toWorkingTime(workingTimeRepository.findByPersonInAndValidFromForDateInterval(persons, start, end));
    }

    @Override
    public Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date) {
        return Optional.ofNullable(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(person, date))
            .map(entity -> toDomain(entity, this::getSystemDefaultFederalState));
    }

    @Override
    public FederalState getFederalStateForPerson(Person person, LocalDate date) {
        return getByPersonAndValidityDateEqualsOrMinorDate(person, date)
            .map(WorkingTime::getFederalState)
            .orElseGet(() -> {
                LOG.debug("No working time found for user '{}' equals or minor {}, using system federal state as fallback",
                    person.getId(), date.format(ofPattern(DD_MM_YYYY)));

                return getSystemDefaultFederalState();
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
        this.touch(defaultWorkingDays, today, person);
    }

    private List<WorkingTime> toWorkingTime(List<WorkingTimeEntity> entities) {
        CachedSupplier<FederalState> federalStateCachedSupplier = new CachedSupplier<>(this::getSystemDefaultFederalState);
        return entities.stream()
            .map(entity -> toDomain(entity, federalStateCachedSupplier))
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

    private static WorkingTime toDomain(WorkingTimeEntity workingTimeEntity, Supplier<FederalState> defaultFederalStateProvider) {

        final FederalState federalState = workingTimeEntity.getFederalStateOverride() == null
            ? defaultFederalStateProvider.get()
            : workingTimeEntity.getFederalStateOverride();

        final WorkingTime workingTime = new WorkingTime(workingTimeEntity.getPerson(), workingTimeEntity.getValidFrom(), federalState);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            final DayLength dayLength = dayLengthForDayOfWeek(workingTimeEntity, dayOfWeek);
            workingTime.setDayLengthForWeekDay(dayOfWeek, dayLength);
        }

        return workingTime;
    }

    private static DayLength dayLengthForDayOfWeek(WorkingTimeEntity workingTimeEntity, DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return workingTimeEntity.getMonday();
            case TUESDAY:
                return workingTimeEntity.getTuesday();
            case WEDNESDAY:
                return workingTimeEntity.getWednesday();
            case THURSDAY:
                return workingTimeEntity.getThursday();
            case FRIDAY:
                return workingTimeEntity.getFriday();
            case SATURDAY:
                return workingTimeEntity.getSaturday();
            case SUNDAY:
                return workingTimeEntity.getSunday();
        }
        return DayLength.ZERO;
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
