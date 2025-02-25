package org.synyx.urlaubsverwaltung.calendar;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

public interface CalendarAbsenceService {

    /**
     * Get absences from a list of persons
     *
     * @param persons to get absences for
     * @return list of absences for the given person
     */
    List<CalendarAbsence> getOpenAbsencesSince(List<Person> persons, LocalDate since);

    /**
     * Get all absences with one of the status:
     * ALLOWED, WAITING, TEMPORARY_ALLOWED
     *
     * @param since
     * @return list of all open absences
     */
    List<CalendarAbsence> getOpenAbsencesSince(LocalDate since);
}
