package org.synyx.urlaubsverwaltung.department;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents a historical membership of a person in a department.
 * Empty {@link DepartmentMembership#validTo()} indicates that the membership is still valid.
 *
 * @param personId {@link org.synyx.urlaubsverwaltung.person.Person} identifier
 * @param departmentId {@link Department} identifier
 * @param membershipKind the kind of membership
 * @param validFrom the start date of the membership
 * @param validTo optional end date of the membership, empty if the membership is still valid
 */
public record DepartmentMembership(
    PersonId personId,
    Long departmentId,
    DepartmentMembershipKind membershipKind,
    Instant validFrom,
    Optional<Instant> validTo
) {

    public DepartmentMembership(PersonId personId, Long departmentId, DepartmentMembershipKind membershipKind, Instant validFrom) {
        this(personId, departmentId, membershipKind, validFrom, Optional.empty());
    }
}
