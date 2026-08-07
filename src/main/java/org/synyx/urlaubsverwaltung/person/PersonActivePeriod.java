package org.synyx.urlaubsverwaltung.person;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a historical period during which a person was active in the system.
 * Empty {@link PersonActivePeriod#validTo()} indicates that the person is still active.
 *
 * @param personId {@link Person} identifier
 * @param validFrom the start date of the active period
 * @param validTo optional end date of the active period, empty if the person is still active
 */
public record PersonActivePeriod(
    PersonId personId,
    Instant validFrom,
    Optional<Instant> validTo
) {

    public PersonActivePeriod(PersonId personId, Instant validFrom) {
        this(personId, validFrom, Optional.empty());
    }
}
