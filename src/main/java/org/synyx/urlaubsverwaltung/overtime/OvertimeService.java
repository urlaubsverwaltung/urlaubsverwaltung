package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides possibility to create and fetch {@link Overtime} records.
 *
 * @since 2.11.0
 */
public interface OvertimeService {

    /**
     * Fetch all the overtime records for a certain person and year.
     *
     * @param person to fetch the overtime records for
     * @param year   to fetch the overtime records for
     * @return list of matching overtime records
     */
    List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year);

    /**
     * Saves an overtime record.
     *
     * @param overtime to be saved
     * @param comment  contains further information to the overtime record, is optional
     * @param author   identifies the person that recorded the overtime
     * @return the created overtime record
     */
    Overtime save(Overtime overtime, Optional<String> comment, Person author);


    /**
     * Fetch the overtime record for a certain ID.
     *
     * @param id to get the overtime record by
     * @return overtime record with the given ID or an empty optional if no entry found for the given ID
     */
    Optional<Overtime> getOvertimeById(Long id);

    /**
     * Fetch the comments for a certain overtime record.
     *
     * @param overtime to get the comments for
     * @return comments to the given overtime record
     */
    List<OvertimeComment> getCommentsForOvertime(Overtime overtime);

    /**
     * Get the total duration of all overtime records of the given person and year.
     *
     * @param person to get the total overtime for
     * @param year   to get the total overtime for
     * @return the total overtime for the given year, never {@code null}
     */
    Duration getTotalOvertimeForPersonAndYear(Person person, int year);

    /**
     * Get the total duration of all overtime records for a person and all years before the given year
     *
     * @param person to get the total overtime for
     * @param year   to get the total overtime before this year (exclusive $year)
     * @return the total overtime for the years, never {@code null}
     */
    Duration getTotalOvertimeForPersonBeforeYear(Person person, int year);

    /**
     * Get the left overtime hours of the given person: the difference between the total overtime and the overtime
     * reduction.
     *
     * @param person to get the left overtime for
     * @return the left overtime, never {@code null}
     * @since 2.13.0
     */
    Duration getLeftOvertimeForPerson(Person person);

    /**
     * Get the left overtime hours of the given person: the difference between the total overtime and the overtime
     * reduction excluding the given applications
     *
     * @param person to get the left overtime for
     * @return the left overtime, never {@code null}
     */
    Duration getLeftOvertimeForPerson(Person person, List<Long> excludingApplicationIDs);

    /**
     * Get the left overtime hours of the given persons: the difference between the total overtime and the overtime
     * reduction.
     *
     * @param persons      to get the left overtime for
     * @param applications to get the left overtime for
     * @param start        of period
     * @param end          of period
     * @return the left overtime
     */
    Map<Person, LeftOvertime> getLeftOvertimeTotalAndDateRangeForPersons(List<Person> persons, List<Application> applications, LocalDate start, LocalDate end);

    /**
     * Is signedInUser allowed to write (create or update) overtime records of given personOfOvertime.
     *
     * @param signedInUser     person which writes overtime record
     * @param personOfOvertime person which the overtime record belongs to
     * @return {@code true} if signedInUser is allowed to write otherwise {@code false}
     */
    boolean isUserIsAllowedToWriteOvertime(Person signedInUser, Person personOfOvertime);

    /**
     * Get all overtime hours of the given person.
     *
     * @param personId id of the given person
     * @return all overtime hours of the given person
     */
    List<Overtime> getAllOvertimesByPersonId(Long personId);
}
