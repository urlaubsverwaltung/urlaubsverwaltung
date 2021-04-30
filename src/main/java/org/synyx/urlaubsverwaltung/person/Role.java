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
    ADMIN,
    INACTIVE;

    public static List<Role> privilegedRoles() {
        return List.of(DEPARTMENT_HEAD, BOSS, OFFICE, SECOND_STAGE_AUTHORITY);
    }
}
