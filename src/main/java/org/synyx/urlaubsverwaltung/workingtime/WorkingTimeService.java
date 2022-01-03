package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkingTimeService {

    /**
     * Returns a list of working times for the given person.
     * <p>
     * Note: The federal state of the working times are either
     * the default federate state based on the settings
     * or the user specific. But never empty.
     *
     * @param person to get all working times
     * @return list of all working times of a person
     */
    List<WorkingTime> getByPerson(Person person);

    /**
     * Returns a map of date ranges and the associated working time.
     * <p>
     * Note: The federal state of the working time is either
     * the default federate state based on the settings
     * or the user specific. But never empty.
     *
     * @param person    to get the working times
     * @param dateRange to specify the
     * @return map of date ranges and the associated working times of a person
     */
    Map<DateRange, WorkingTime> getWorkingTimesByPersonAndDateRange(Person person, DateRange dateRange);

    /**
     * Returns a map of date ranges and the associated federal state.
     * <p>
     * Note: The federal state of the {@link DateRange} is either
     * the default federate state based on the settings
     * or the user specific. But never empty.
     *
     * @param person    to get the federal states
     * @param dateRange to specify the
     * @return map of date ranges and the associated federal state of a person
     */
    Map<DateRange, FederalState> getFederalStatesByPersonAndDateRange(Person person, DateRange dateRange);

    List<WorkingTime> getByPersons(List<Person> persons);

    Optional<WorkingTime> getByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);

    FederalState getFederalStateForPerson(Person person, LocalDate date);

    FederalState getSystemDefaultFederalState();
}
