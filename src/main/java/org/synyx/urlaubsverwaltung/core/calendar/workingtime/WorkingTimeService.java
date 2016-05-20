package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.util.DateFormat;

import java.util.List;
import java.util.Optional;


/**
 * Service for handling {@link WorkingTime} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class WorkingTimeService {

    private static final Logger LOG = Logger.getLogger(WorkingTimeService.class);

    private final WorkingTimeDAO workingTimeDAO;
    private final SettingsService settingsService;

    @Autowired
    public WorkingTimeService(WorkingTimeDAO workingTimeDAO, SettingsService settingsService) {

        this.workingTimeDAO = workingTimeDAO;
        this.settingsService = settingsService;
    }

    public void touch(List<Integer> workingDays, Optional<FederalState> federalState, DateMidnight validFrom,
        Person person) {

        WorkingTime workingTime = workingTimeDAO.findByPersonAndValidityDate(person, validFrom.toDate());

        /*
         * create a new WorkingTime object if no one existent for the given person and date
         */
        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.setPerson(person);
            workingTime.setValidFrom(validFrom);
        }

        /**
         * else just change the working days of the current working time object
         */
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        if (federalState.isPresent()) {
            workingTime.setFederalStateOverride(federalState.get());
        }

        workingTimeDAO.save(workingTime);
    }


    public List<WorkingTime> getByPerson(Person person) {

        return workingTimeDAO.findByPerson(person);
    }


    public Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, DateMidnight date) {

        return Optional.ofNullable(workingTimeDAO.findByPersonAndValidityDateEqualsOrMinorDate(person, date.toDate()));
    }


    public Optional<WorkingTime> getCurrentOne(Person person) {

        return Optional.ofNullable(workingTimeDAO.findLastOneByPerson(person));
    }


    public FederalState getFederalStateForPerson(Person person, DateMidnight date) {

        Optional<WorkingTime> optionalWorkingTime = getByPersonAndValidityDateEqualsOrMinorDate(person, date);

        if (!optionalWorkingTime.isPresent()) {
            LOG.debug(String.format(
                    "No working time found for user '%s' equals or minor %s, thus using system federal state as fallback",
                    person.getLoginName(), date.toString(DateFormat.PATTERN)));

            return getSystemDefaultFederalState();
        }

        return getFederalState(optionalWorkingTime.get());
    }


    private FederalState getFederalState(WorkingTime workingTime) {

        if (workingTime.getFederalStateOverride().isPresent()) {
            return workingTime.getFederalStateOverride().get();
        }

        return getSystemDefaultFederalState();
    }


    private FederalState getSystemDefaultFederalState() {

        return settingsService.getSettings().getWorkingTimeSettings().getFederalState();
    }
}
