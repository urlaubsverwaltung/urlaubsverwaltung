package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_COMMENT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

/**
 * Permissions of a user on sick notes of one certain person, see
 * {@link SickNotePermissionEvaluator#of(Person, Person)}.
 *
 * <p>Besides the roles of the user the rules depend on facts that cannot be derived from a {@link Person}: whether the
 * user is a department head or a second stage authority of the person and whether handing in sick notes is enabled at
 * all. They are passed in as a snapshot taken by the evaluator, therefore an instance is meant to live no longer than
 * one request.
 */
public final class SickNotePermissions {

    private final Person signedInUser;
    private final Person sickNotePerson;
    private final boolean departmentHeadOfPerson;
    private final boolean secondStageAuthorityOfPerson;
    private final boolean submissionOfSickNotesEnabled;

    SickNotePermissions(Person signedInUser, Person sickNotePerson, boolean departmentHeadOfPerson,
                        boolean secondStageAuthorityOfPerson, boolean submissionOfSickNotesEnabled) {
        this.signedInUser = signedInUser;
        this.sickNotePerson = sickNotePerson;
        this.departmentHeadOfPerson = departmentHeadOfPerson;
        this.secondStageAuthorityOfPerson = secondStageAuthorityOfPerson;
        this.submissionOfSickNotesEnabled = submissionOfSickNotesEnabled;
    }

    /**
     * @return {@code true} if the user may see the sick notes of the person, {@code false} otherwise
     */
    public boolean isAllowedToView() {
        return isSamePerson()
            || signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(BOSS) && signedInUser.hasRole(SICK_NOTE_VIEW))
            || isManagerOfPerson();
    }

    /**
     * @return {@code true} if the user may create an already accepted sick note for the person, {@code false} otherwise
     */
    public boolean isAllowedToAdd() {
        return isAllowedToMaintain(SICK_NOTE_ADD);
    }

    /**
     * Whether the user may hand in a sick note for the person. Only the person itself may do so and only if handing in
     * sick notes is enabled in the settings.
     *
     * @return {@code true} if the user may submit a sick note for the person, {@code false} otherwise
     */
    public boolean isAllowedToSubmit() {
        return isSamePerson() && submissionOfSickNotesEnabled;
    }

    /**
     * @return {@code true} if the user may accept a submitted sick note of the person, {@code false} otherwise
     */
    public boolean isAllowedToAccept() {
        return isAllowedToMaintain(SICK_NOTE_EDIT);
    }

    /**
     * Accepting a submitted extension changes the end date of an existing sick note and is therefore governed by the
     * very same rule as accepting a sick note.
     *
     * @return {@code true} if the user may accept a submitted extension of the person, {@code false} otherwise
     */
    public boolean isAllowedToAcceptExtension() {
        return isAllowedToAccept();
    }

    /**
     * @param sickNote sick note to be edited, has to belong to the person of these permissions
     * @return {@code true} if the user may edit the given sick note, {@code false} otherwise
     */
    public boolean isAllowedToEdit(SickNote sickNote) {
        return isAllowedToMaintain(SICK_NOTE_EDIT)
            || (isSamePerson() && sickNote.isSubmitted());
    }

    /**
     * @return {@code true} if the user may convert a sick note of the person into an application for leave,
     * {@code false} otherwise
     */
    public boolean isAllowedToConvert() {
        return signedInUser.hasRole(OFFICE);
    }

    /**
     * @return {@code true} if the user may cancel a sick note of the person, {@code false} otherwise
     */
    public boolean isAllowedToCancel() {
        return isAllowedToMaintain(SICK_NOTE_CANCEL);
    }

    /**
     * @return {@code true} if the user may comment a sick note of the person, {@code false} otherwise
     */
    public boolean isAllowedToComment() {
        return isAllowedToMaintain(SICK_NOTE_COMMENT);
    }

    private boolean isAllowedToMaintain(Role role) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(role) && (signedInUser.hasRole(BOSS) || isManagerOfPerson()));
    }

    private boolean isManagerOfPerson() {
        return departmentHeadOfPerson || secondStageAuthorityOfPerson;
    }

    private boolean isSamePerson() {
        // signedInUser first, the person of a sick note form may not have been submitted at all
        return signedInUser.equals(sickNotePerson);
    }
}
