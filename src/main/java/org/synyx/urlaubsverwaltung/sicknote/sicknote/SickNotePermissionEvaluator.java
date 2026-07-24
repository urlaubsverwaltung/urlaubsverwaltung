package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.person.Person;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_CANCEL;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_COMMENT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_EDIT;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

public final class SickNotePermissionEvaluator {

    private SickNotePermissionEvaluator() {
        // ok
    }

    static boolean isAllowedToViewSickNote(SickNote sickNote, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return sickNote.getPerson().equals(signedInUser)
            || signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_VIEW) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson))
            || isDepartmentHeadOfPerson
            || isSecondStageAuthorityOfPerson;
    }

    static boolean isAllowedToAddSickNote(Person sickNotePerson, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson, boolean isSubmitEnabled) {
        return isAllowedToAddSickNoteFor(signedInUser, isDepartmentHeadOfPerson, isSecondStageAuthorityOfPerson)
            || (sickNotePerson.equals(signedInUser) && isSubmitEnabled);
    }

    static boolean isAllowedToAddSickNoteFor(Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_ADD) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }

    static boolean isAllowedToAcceptSickNoteExtension(SickNote sickNote, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_ADD) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }

    static boolean isAllowedToAcceptSickNote(Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_EDIT) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }

    static boolean isAllowedToEditSickNote(SickNote sickNote, Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_EDIT) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson))
            || (sickNote.getPerson().equals(signedInUser) && sickNote.isSubmitted());
    }

    static boolean isAllowedToConvertSickNote(Person signedInUser) {
        return signedInUser.hasRole(OFFICE);
    }

    static boolean isAllowedToCancelSickNote(Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_CANCEL) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }

    static boolean isAllowedToCommentSickNote(Person signedInUser, boolean isDepartmentHeadOfPerson, boolean isSecondStageAuthorityOfPerson) {
        return signedInUser.hasRole(OFFICE)
            || (signedInUser.hasRole(SICK_NOTE_COMMENT) && (signedInUser.hasRole(BOSS) || isDepartmentHeadOfPerson || isSecondStageAuthorityOfPerson));
    }
}
