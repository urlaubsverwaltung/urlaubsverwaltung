package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkingTimeService {

    List<WorkingTime> getByPerson(Person person);

    Map<DateRange, FederalState> getFederalStatesByPersonAndDateRange(Person person, DateRange dateRange);

    List<WorkingTime> getByPersons(List<Person> persons);

    Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);

    FederalState getFederalStateForPerson(Person person, LocalDate date);

    FederalState getSystemDefaultFederalState();
}
