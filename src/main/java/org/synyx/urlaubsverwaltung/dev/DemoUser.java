package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.Role;

/**
 * Demo users that can be used to sign in with when demo data is created.
 */
enum DemoUser {

    USER("user", "user@urlaubsverwaltung.cloud", Role.USER),
    DEPARTMENT_HEAD("departmentHead", "departmentHead@urlaubsverwaltung.cloud", Role.USER, Role.DEPARTMENT_HEAD),
    SECOND_STAGE_AUTHORITY("secondStageAuthority", "secondStageAuthority@urlaubsverwaltung.cloud", Role.USER, Role.SECOND_STAGE_AUTHORITY),
    BOSS("boss", "boss@urlaubsverwaltung.cloud", Role.USER, Role.BOSS),
    OFFICE("office", "office@urlaubsverwaltung.cloud", Role.USER, Role.OFFICE),
    ADMIN("admin", "admin@urlaubsverwaltung.cloud", Role.USER, Role.ADMIN);

    private final String username;

    private final String email;
    private final Role[] roles;

    DemoUser(String username, String email, Role... roles) {
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    Role[] getRoles() {
        return roles;
    }
}
