package org.synyx.urlaubsverwaltung.department;

import org.synyx.urlaubsverwaltung.person.PersonId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * A bucket that contains all current memberships of a department.
 * It contains members, department heads and second stage authorities.
 */
public record DepartmentMembershipBucket(
    Long departmentId,
    List<DepartmentMembership> members,
    List<DepartmentMembership> departmentHeads,
    List<DepartmentMembership> secondStageAuthorities
) {

    /**
     * Checks if the given person is a {@link org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} of the department.
     *
     * @param personId the person to check
     * @return {@code true} if the person is a department head, {@code false} otherwise
     */
    public boolean isInDepartmentHeads(PersonId personId) {
        return departmentHeads.stream()
            .anyMatch(departmentHead -> departmentHead.personId().equals(personId));
    }

    /**
     * Checks if the given person is a {@link org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY} of the department.
     *
     * @param personId the person to check
     * @return {@code true} if the person is a second stage authority, {@code false} otherwise
     */
    public boolean isInSecondStageAuthorities(PersonId personId) {
        return secondStageAuthorities.stream()
            .anyMatch(secondStageAuthority -> secondStageAuthority.personId().equals(personId));
    }

    public static DepartmentMembershipBucket empty(Long departmentId) {
        return new DepartmentMembershipBucket(departmentId, List.of(), List.of(), List.of());
    }

    /**
     * Creates a new {@link DepartmentMembershipBucket} from a list of memberships which will be categorized into members,
     * department heads, and second stage authorities based on their membership kind.
     *
     * @param departmentId the ID of the department
     * @param memberships the list of memberships to categorize
     * @return a new {@link DepartmentMembershipBucket} containing categorized memberships
     */
    public static DepartmentMembershipBucket ofMemberships(Long departmentId, List<DepartmentMembership> memberships) {

        final List<DepartmentMembership> members = new ArrayList<>();
        final List<DepartmentMembership> departmentHeads = new ArrayList<>();
        final List<DepartmentMembership> secondStageAuthorities = new ArrayList<>();

        for (DepartmentMembership membership : memberships) {
            final List<DepartmentMembership> bucket = switch (membership.membershipKind()) {
                case MEMBER -> members;
                case DEPARTMENT_HEAD -> departmentHeads;
                case SECOND_STAGE_AUTHORITY -> secondStageAuthorities;
            };
            bucket.add(membership);
        }

        return new DepartmentMembershipBucket(
            departmentId,
            unmodifiableList(members),
            unmodifiableList(departmentHeads),
            unmodifiableList(secondStageAuthorities)
        );
    }

    public Set<PersonId> allPersonIds() {

        final List<PersonId> personIds = new ArrayList<>();
        members.stream().map(DepartmentMembership::personId).forEach(personIds::add);
        departmentHeads.stream().map(DepartmentMembership::personId).forEach(personIds::add);
        secondStageAuthorities.stream().map(DepartmentMembership::personId).forEach(personIds::add);

        return personIds.stream().collect(toUnmodifiableSet());
    }
}
