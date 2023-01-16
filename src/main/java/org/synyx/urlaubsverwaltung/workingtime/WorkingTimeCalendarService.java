package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Year;
import java.util.Collection;
import java.util.Map;

public interface WorkingTimeCalendarService {

    /**
     * Returns a map of persons and the associated {@link WorkingTimeCalendar}.
     *
     * @param persons to get the WorkingTimeCalendar
     * @param year to get the WorkingTimeCalendar
     * @return map of persons and the associated {@link WorkingTimeCalendar}.
     */
    Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, Year year);

    /**
     * Returns a map of persons and the associated {@link WorkingTimeCalendar}.
     *
     * @param persons to get the WorkingTimeCalendar
     * @param dateRange to get the WorkingTimeCalendar
     * @return map of persons and the associated {@link WorkingTimeCalendar}.
     */
    Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, DateRange dateRange);
}
