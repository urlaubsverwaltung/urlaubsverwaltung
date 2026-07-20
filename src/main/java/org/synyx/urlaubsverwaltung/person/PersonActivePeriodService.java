package org.synyx.urlaubsverwaltung.person;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides read access to the historical periods during which persons have been active in the system.
 */
public interface PersonActivePeriodService {

    /**
     * Returns all active periods of a person, ordered from the earliest to the most recent.
     *
     * @param personId the ID of the person for whom to retrieve the active periods
     * @return all {@link PersonActivePeriod} of the person, ordered by {@link PersonActivePeriod#validFrom()} ascending
     */
    List<PersonActivePeriod> getActivePeriods(PersonId personId);

    /**
     * Returns all active periods of the given persons that overlap with the given time range.
     *
     * @param personIds the IDs of the persons for whom to retrieve the active periods
     * @param from the (inclusive) start of the time range
     * @param to the (exclusive) end of the time range
     * @return map of overlapping {@link PersonActivePeriod} by {@link PersonId}, containing an empty list for
     *     persons without an overlapping active period
     */
    Map<PersonId, List<PersonActivePeriod>> getActivePeriodsOverlapping(Collection<PersonId> personIds, Instant from, Instant to);

    /**
     * Returns all active periods of all persons, regardless of person. Intended for bulk consumers such as backups.
     *
     * @return all {@link PersonActivePeriod}
     */
    List<PersonActivePeriod> getAllActivePeriods();
}
