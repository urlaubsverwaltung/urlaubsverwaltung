package org.synyx.urlaubsverwaltung.workingtime;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface WorkingTimeCalendarService {

    /**
     * Returns a map of persons and the associated {@link WorkingTimeCalendar}.
     *
     * @param persons to get the WorkingTimeCalendar
     * @param year    to get the WorkingTimeCalendar
     * @return map of persons and the associated {@link WorkingTimeCalendar}.
     */
    Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, Year year);

    /**
     * Returns a map of persons and the associated {@link WorkingTimeCalendar}.
     *
     * @param persons   to get the WorkingTimeCalendar
     * @param dateRange to get the WorkingTimeCalendar
     * @return map of persons and the associated {@link WorkingTimeCalendar}.
     */
    Map<Person, WorkingTimeCalendar> getWorkingTimesByPersons(Collection<Person> persons, DateRange dateRange);

    /**
     * Calculates the next date of a working day.
     *
     * <p>
     * Examples given working days are MONDAY, TUESDAY and FRIDAY
     * <ul>
     *     <li>{@code monday} -> tuesday</li>
     *     <li>{@code tuesday} -> friday</li>
     *     <li>{@code wednesday} -> friday</li>
     *     <li>{@code thursday} -> friday</li>
     *     <li>{@code friday} -> monday</li>
     * </ul>
     *
     * @param person person to use
     * @param date any {@linkplain LocalDate}
     * @return optional resolving to a {@linkplain LocalDate} of the next working day when it exists.
     */
    Optional<LocalDate> getNextWorkingDayFollowingTo(Person person, LocalDate date);
}
