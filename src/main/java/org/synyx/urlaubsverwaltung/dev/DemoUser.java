package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.Role;

/**
 * Demo users that can be used to sign in with when demo data is created.
 */
enum DemoUser {

    USER("user", "secret", Role.USER),
    DEPARTMENT_HEAD("departmentHead", "secret", Role.USER, Role.DEPARTMENT_HEAD),
    SECOND_STAGE_AUTHORITY("secondStageAuthority", "secret", Role.USER, Role.SECOND_STAGE_AUTHORITY),
    BOSS("boss", "secret", Role.USER, Role.BOSS),
    OFFICE("office", "secret", Role.USER, Role.BOSS, Role.OFFICE),
    ADMIN("admin", "secret", Role.USER, Role.ADMIN);

    private final String username;
    private final String password;
    private final Role[] roles;

    DemoUser(String username, String password, Role... roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    String getUsername() {
        return username;
    }

    Role[] getRoles() {
        return roles;
    }

    String getPassword() {
        return password;
    }
}
