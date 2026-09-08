package org.synyx.urlaubsverwaltung.overtime;

import org.synyx.urlaubsverwaltung.person.Person;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Permissions of a user on the overtime of one certain person, see
 * {@link OvertimePermissionEvaluator#of(Person, Person)}.
 *
 * <p>Besides the roles of the user the rules depend on facts that cannot be derived from a {@link Person}: whether the
 * user is a department head or a second stage authority of the person and how overtime is configured. They are passed
 * in as a snapshot taken by the evaluator, therefore an instance is meant to live no longer than one request.
 */
public final class OvertimePermissions {

    private final Person signedInUser;
    private final Person overtimePerson;
    private final boolean departmentHeadOfPerson;
    private final boolean secondStageAuthorityOfPerson;
    private final boolean overtimeActive;
    private final boolean overtimeSyncActive;
    private final boolean overtimeWritePrivilegedOnly;

    OvertimePermissions(Person signedInUser, Person overtimePerson, boolean departmentHeadOfPerson,
                        boolean secondStageAuthorityOfPerson, boolean overtimeActive, boolean overtimeSyncActive,
                        boolean overtimeWritePrivilegedOnly) {
        this.signedInUser = signedInUser;
        this.overtimePerson = overtimePerson;
        this.departmentHeadOfPerson = departmentHeadOfPerson;
        this.secondStageAuthorityOfPerson = secondStageAuthorityOfPerson;
        this.overtimeActive = overtimeActive;
        this.overtimeSyncActive = overtimeSyncActive;
        this.overtimeWritePrivilegedOnly = overtimeWritePrivilegedOnly;
    }

    /**
     * Whether the user may see the overtime of the person. Everyone sees their own,
     * {@link org.synyx.urlaubsverwaltung.person.Role#OFFICE} and
     * {@link org.synyx.urlaubsverwaltung.person.Role#BOSS} see everyone and a manager sees the members they are
     * responsible for. Reading does not depend on overtime being switched on - the records stay visible.
     *
     * @return {@code true} if the user may see the overtime of the person, {@code false} otherwise
     */
    public boolean isAllowedToView() {
        return isSamePerson()
            || signedInUser.hasRole(OFFICE)
            || signedInUser.hasRole(BOSS)
            || isManagerOfPerson();
    }

    private boolean isManagerOfPerson() {
        return departmentHeadOfPerson || secondStageAuthorityOfPerson;
    }

    private boolean isSamePerson() {
        return signedInUser.equals(overtimePerson);
    }
}
