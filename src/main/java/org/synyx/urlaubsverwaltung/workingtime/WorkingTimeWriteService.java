package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

public interface WorkingTimeWriteService {

    void touch(List<Integer> workingDays, LocalDate validFrom, Person person);

    void touch(List<Integer> workingDays, LocalDate validFrom, Person person, FederalState federalState);

    void createDefaultWorkingTime(Person person);

    void deleteAllByPerson(Person person);
}
