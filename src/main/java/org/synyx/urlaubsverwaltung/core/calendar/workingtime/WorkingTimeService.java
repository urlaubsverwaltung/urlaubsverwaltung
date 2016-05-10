package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;

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

    private final WorkingTimeDAO workingTimeDAO;

    @Autowired
    public WorkingTimeService(WorkingTimeDAO workingTimeDAO) {

        this.workingTimeDAO = workingTimeDAO;
    }

    public void touch(List<Integer> workingDays, Optional<FederalState> federalState, DateMidnight validFrom, Person person) {

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

        workingTime.setFederalStateOverride(federalState);

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
}
