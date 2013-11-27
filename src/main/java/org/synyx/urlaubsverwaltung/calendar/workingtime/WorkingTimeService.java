package org.synyx.urlaubsverwaltung.calendar.workingtime;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * Service for handling {@link WorkingTime} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class WorkingTimeService {

    private WorkingTimeDAO workingTimeDAO;

    @Autowired
    public WorkingTimeService(WorkingTimeDAO workingTimeDAO) {

        this.workingTimeDAO = workingTimeDAO;
    }


    public WorkingTimeService() {
    }

    public void touch(List<Integer> workingDays, Person person) {

        WorkingTime workingTime = getCurrentOne(person);

        /*
         * create a new WorkingTime object if no one existent for the person
         * or if the properties (working days) differ from the current WorkingTime object
         */
        if (newOneMustBeCreated(workingTime, workingDays)) {
            workingTime = new WorkingTime();
            workingTime.setPerson(person);
            workingTime.setValidFrom(DateMidnight.now());
        }

        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        workingTimeDAO.save(workingTime);
    }


    /**
     * If the given {@link WorkingTime} is null or if its working days and validFrom date would be changed, a new
     * {@link WorkingTime} object has to be created.
     *
     * @param  workingTime
     * @param  workingDays
     *
     * @return  true if a new {@link WorkingTime} object has to be created, false if the {@link WorkingTime} object can
     *          be edited
     */
    private boolean newOneMustBeCreated(WorkingTime workingTime, List<Integer> workingDays) {

        if (workingTime == null) {
            return true;
        }

        DateMidnight validFrom = workingTime.getValidFrom();
        DateMidnight now = DateMidnight.now();

        if ((!workingTime.hasWorkingDays(workingDays) && !validFrom.isEqual(now))) {
            return true;
        }

        return false;
    }


    public List<WorkingTime> getByPerson(Person person) {

        return workingTimeDAO.findByPerson(person);
    }


    public WorkingTime getByPersonAndValidityDate(Person person, DateMidnight date) {

        return workingTimeDAO.findByPersonAndValidityDate(person, date.toDate());
    }


    public WorkingTime getCurrentOne(Person person) {

        return workingTimeDAO.findLastOneByPerson(person);
    }
}
