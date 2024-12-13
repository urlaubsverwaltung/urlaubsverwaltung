package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.Role;

public enum RoleDTO {
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

    public Role toRole() {
        return Role.valueOf(this.name());
    }
}
