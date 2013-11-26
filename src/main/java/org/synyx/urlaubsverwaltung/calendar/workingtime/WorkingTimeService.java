package org.synyx.urlaubsverwaltung.calendar.workingtime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Arrays;
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

    public void create(Integer[] workingDaysArray, Person person) {

        List<Integer> workingDays = Arrays.asList(workingDaysArray);
        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, DayLength.FULL);
        workingTime.setPerson(person);

        workingTimeDAO.save(workingTime);
    }
}
