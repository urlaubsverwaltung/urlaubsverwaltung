package org.synyx.urlaubsverwaltung.calendar.workingtime;

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

        WorkingTime workingTime = getByPerson(person);

        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.setPerson(person);
        }

        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        workingTimeDAO.save(workingTime);
    }


    public WorkingTime getByPerson(Person person) {

        return workingTimeDAO.findByPerson(person);
    }
}
