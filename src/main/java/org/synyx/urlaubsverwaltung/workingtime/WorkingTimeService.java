package org.synyx.urlaubsverwaltung.workingtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.util.DateFormat.DD_MM_YYYY;

@Service
@Transactional
public class WorkingTimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final WorkingTimeProperties workingTimeProperties;
    private final WorkingTimeRepository workingTimeRepository;
    private final SettingsService settingsService;
    private final Clock clock;

    @Autowired
    public WorkingTimeService(WorkingTimeProperties workingTimeProperties, WorkingTimeRepository workingTimeRepository, SettingsService settingsService, Clock clock) {
        this.workingTimeProperties = workingTimeProperties;
        this.workingTimeRepository = workingTimeRepository;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    public void touch(List<Integer> workingDays, Optional<FederalState> federalState, LocalDate validFrom, Person person) {

        WorkingTime workingTime = workingTimeRepository.findByPersonAndValidityDate(person, validFrom);

        /*
         * create a new WorkingTime object if no one existent for the given person and date
         */
        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.setPerson(person);
            workingTime.setValidFrom(validFrom);
        }

        /*
         * else just change the working days of the current working time object
         */
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        if (federalState.isPresent()) {
            workingTime.setFederalStateOverride(federalState.get());
        } else {
            // reset federal state override, use system default federal state for this user
            workingTime.setFederalStateOverride(null);
        }

        workingTimeRepository.save(workingTime);
        LOG.info("Created working time {} for person {}", workingTime, person);
    }

    public List<WorkingTime> getByPerson(Person person) {
        return workingTimeRepository.findByPersonOrderByValidFromDesc(person);
    }

    public List<WorkingTime> getByPersonsAndDateInterval(List<Person> persons, LocalDate start, LocalDate end) {
        return workingTimeRepository.findByPersonInAndValidFromForDateInterval(persons, start, end);
    }

    public Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date) {
        return Optional.ofNullable(workingTimeRepository.findByPersonAndValidityDateEqualsOrMinorDate(person, date));
    }

    public FederalState getFederalStateForPerson(Person person, LocalDate date) {
        Optional<WorkingTime> optionalWorkingTime = getByPersonAndValidityDateEqualsOrMinorDate(person, date);

        if (optionalWorkingTime.isEmpty()) {
            LOG.debug("No working time found for user '{}' equals or minor {}, using system federal state as fallback",
                person.getId(), date.format(ofPattern(DD_MM_YYYY)));

            return getSystemDefaultFederalState();
        }

        return getFederalState(optionalWorkingTime.get());
    }

    private FederalState getFederalState(WorkingTime workingTime) {
        Optional<FederalState> optionalFederalStateOverride = workingTime.getFederalStateOverride();
        return optionalFederalStateOverride.orElseGet(this::getSystemDefaultFederalState);
    }

    public FederalState getSystemDefaultFederalState() {
        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }

    public void createDefaultWorkingTime(Person person) {
        final List<Integer> defaultWorkingDays;

        if (workingTimeProperties.isDefaultWorkingDaysDeactivated()) {
            defaultWorkingDays = settingsService.getSettings().getWorkingTimeSettings().getWorkingDays();
        } else {
            defaultWorkingDays = workingTimeProperties.getDefaultWorkingDays();
        }

        final LocalDate today = LocalDate.now(clock);
        this.touch(defaultWorkingDays, Optional.empty(), today, person);
    }
}
