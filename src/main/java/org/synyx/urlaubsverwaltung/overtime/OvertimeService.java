package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


/**
 * Provides possibility to create and fetch {@link Overtime} records.
 *
 * @since 2.11.0
 */
public interface OvertimeService {

    /**
     * Fetch all the overtime records for a certain person.
     *
     * @param person to fetch the overtime records for
     * @return list of overtime records the person has
     */
    List<Overtime> getOvertimeRecordsForPerson(Person person);


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
    Overtime record(Overtime overtime, Optional<String> comment, Person author);


    /**
     * Fetch the overtime record for a certain ID.
     *
     * @param id to get the overtime record by
     * @return overtime record with the given ID or an empty optional if no entry found for the given ID
     */
    Optional<Overtime> getOvertimeById(Integer id);


    /**
     * Fetch the comments for a certain overtime record.
     *
     * @param overtime to get the comments for
     * @return comments to the given overtime record
     */
    List<OvertimeComment> getCommentsForOvertime(Overtime overtime);


    /**
     * Get the total hours of all overtime records of the given person and year.
     *
     * @param person to get the total overtime for
     * @param year   to get the total overtime for
     * @return the total overtime for the given year, never {@code null}
     */
    BigDecimal getTotalOvertimeForPersonAndYear(Person person, int year);


    /**
     * Get the left overtime hours of the given person: the difference between the total overtime and the overtime
     * reduction.
     *
     * @param person to get the left overtime for
     * @return the left overtime, never {@code null}
     * @since 2.13.0
     */
    BigDecimal getLeftOvertimeForPerson(Person person);
}
