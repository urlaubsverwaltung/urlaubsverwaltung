package org.synyx.urlaubsverwaltung.absence;


import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;


public interface AbsenceService {

    /**
     * Get absences from a list of persons
     *
     * @param persons to get absences for
     * @return list of absences for the given person
     */
    List<Absence> getOpenAbsencesSince(List<Person> persons, LocalDate since);

    /**
     * Get all absences with one of the status:
     * ALLOWED, WAITING, TEMPORARY_ALLOWED
     *
     * @param since
     * @return list of all open absences
     */
    List<Absence> getOpenAbsencesSince(LocalDate since);
}
