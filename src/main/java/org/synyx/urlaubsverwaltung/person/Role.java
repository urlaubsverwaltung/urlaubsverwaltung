package org.synyx.urlaubsverwaltung.person;

import java.util.List;

/**
 * Enum describing possible types of rights/roles a user may have.
 */
public enum Role {

    USER,
    DEPARTMENT_HEAD,
    SECOND_STAGE_AUTHORITY,
    BOSS,
    OFFICE,
    INACTIVE,
    APPLICATION_ADD,
    APPLICATION_CANCEL,
    APPLICATION_CANCELLATION_REQUESTED,
    PERSON_ADD,
    SICK_NOTE_VIEW,
    SICK_NOTE_ADD,
    SICK_NOTE_EDIT,
    SICK_NOTE_CANCEL,
    SICK_NOTE_COMMENT;

    public static List<Role> privilegedRoles() {
        return List.of(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY, BOSS, OFFICE);
    }
}
