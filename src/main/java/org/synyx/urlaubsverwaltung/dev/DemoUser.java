package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.Role;

/**
 * Demo users that can be used to sign in with when demo data is created.
 */
enum DemoUser {

    USER("user", Role.USER),
    DEPARTMENT_HEAD("departmentHead", Role.USER, Role.DEPARTMENT_HEAD),
    SECOND_STAGE_AUTHORITY("secondStageAuthority", Role.USER, Role.SECOND_STAGE_AUTHORITY),
    BOSS("boss", Role.USER, Role.BOSS),
    OFFICE("office", Role.USER, Role.BOSS, Role.OFFICE),
    ADMIN("admin", Role.USER, Role.ADMIN);

    public static final String SECRET = "{pbkdf2}7fa3ad492f74237b2fae54a07d81df1b17e5b3790fa629eb1b3fea19f86d4fccb62cf12bcfbd5913";

    private final String username;
    private final Role[] roles;

    DemoUser(String username, Role... roles) {
        this.username = username;
        this.roles = roles;
    }

    String getUsername() {
        return username;
    }

    Role[] getRoles() {
        return roles;
    }

    String getPasswordHash() {
        return SECRET;
    }
}
