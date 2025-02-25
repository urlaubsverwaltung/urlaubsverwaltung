package org.synyx.urlaubsverwaltung.absence;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

public interface AbsenceService {

    /**
     * Get all open absences for the given person and date range.
     * "Open" means it has one of the status WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED
     *
     * @param person {@link Person} to get the absences for
     * @param start  start of the date range (inclusive)
     * @param end    end of the date range (inclusive)
     * @return list of all matching absences
     */
    List<AbsencePeriod> getOpenAbsences(Person person, LocalDate start, LocalDate end);

    /**
     * Get all open absences for the given persons and date range.
     * "Open" means it has one of the status WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED
     *
     * @param persons list of {@link Person}s to get the absences for
     * @param start   start of the date range (inclusive)
     * @param end     end of the date range (inclusive)
     * @return list of all matching absences
     */
    List<AbsencePeriod> getOpenAbsences(List<Person> persons, LocalDate start, LocalDate end);

    /**
     * Get all closed absences for the given person and date range.
     * "Closed" means it has one of the status REVOKED, REJECTED, CANCELLED
     *
     * @param person {@link Person} to get the absences for
     * @param start  start of the date range (inclusive)
     * @param end    end of the date range (inclusive)
     * @return list of all matching absences
     */
    List<AbsencePeriod> getClosedAbsences(Person person, LocalDate start, LocalDate end);

    /**
     * Get all closed absences for the given persons and date range.
     * "closed" means it has one of the status REVOKED, REJECTED, CANCELLED
     *
     * @param persons list of {@link Person}s to get the absences for
     * @param start   start of the date range (inclusive)
     * @param end     end of the date range (inclusive)
     * @return list of all matching absences
     */
    List<AbsencePeriod> getClosedAbsences(List<Person> persons, LocalDate start, LocalDate end);
}
