package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling {@link SickNote}s.
 */
public interface SickNoteService {

    /**
     * Persists the given sick note.
     *
     * @param sickNote to be persisted
     * @return saved sick note
     */
    SickNote save(SickNote sickNote);

    /**
     * Gets the sick note with the given id.
     *
     * @param id to search the sick note by
     * @return optional sick note matching the given id
     */
    Optional<SickNote> getById(Long id);

    /**
     * Get all the sick notes of the given person that are in the given period.
     *
     * @param person defines the owner of the sick notes
     * @param from   defines the start of the period
     * @param to     defines the end of the period
     * @return all the sick notes matching the given parameters
     */
    List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to);

    Optional<SickNote> getSickNoteOfYesterdayOrLastWorkDay(Person person);

    /**
     * Get all the sick notes that are reaching the end of sick pay.
     *
     * @return sick notes that are reaching the end of sick pay
     */
    List<SickNote> getSickNotesReachingEndOfSickPay();

    List<SickNote> getAllActiveByPeriod(LocalDate from, LocalDate to);

    /**
     * Get all {@link SickNote} with specific states since
     *
     * @return all {@link SickNote} with specific states since
     */
    List<SickNote> getForStatesSince(List<SickNoteStatus> sickNoteStatuses, LocalDate since);

    /**
     * Get all {@link SickNote} with specific states and persons
     *
     * @return all {@link SickNote} with specific states and persons
     */
    List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatuses, List<Person> persons);

    /**
     * Get all {@link SickNote} with specific states and persons
     *
     * @return all {@link SickNote} with specific states and persons
     */
    List<SickNote> getForStatesAndPersonSince(List<SickNoteStatus> sickNoteStatuses, List<Person> persons, LocalDate since);

    /**
     * Get all {@link SickNote}s with specific states and persons for the given date range
     *
     * @param sickNoteStatus {@link SickNoteStatus} to filter
     * @param persons        {@link Person}s to consider
     * @param start          start date (inclusive)
     * @param end            end date (inclusive)
     * @return list of all matching {@link SickNote}s
     */
    List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate start, LocalDate end);

    List<SickNote> getForStatesAndPersonAndPersonHasRoles(List<SickNoteStatus> sickNoteStatus, List<Person> persons, List<Role> roles, LocalDate start, LocalDate end);

    /**
     * Set end of sick pay notification send for given sicknote.
     *
     * @param sickNote to set sick pay notification send date
     */
    void setEndOfSickPayNotificationSend(SickNote sickNote);

    /**
     * Deletes all {@link SickNote} in the database person.
     *
     * @param person the person whose sick notes should be deleted
     * @return list of deleted sick notes
     */
    List<SickNote> deleteAllByPerson(Person person);

    /**
     * Deletes the given applier from all sicknotes as applier
     *
     * @param applier to delete
     */
    void deleteSickNoteApplier(Person applier);
}
