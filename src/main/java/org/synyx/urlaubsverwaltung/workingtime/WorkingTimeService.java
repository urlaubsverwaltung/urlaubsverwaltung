package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkingTimeService {

    void touch(List<Integer> workingDays, Optional<FederalState> federalState, LocalDate validFrom, Person person);

    List<WorkingTime> getByPerson(Person person);

    List<WorkingTime> getByPersonsAndDateInterval(List<Person> persons, LocalDate start, LocalDate end);

    Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);

    FederalState getFederalStateForPerson(Person person, LocalDate date);

    FederalState getSystemDefaultFederalState();

    void createDefaultWorkingTime(Person person);
}
