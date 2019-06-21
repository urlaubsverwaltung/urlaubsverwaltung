package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.person.Role;


/**
 * Test users that can be used to sign in with when test data is created.
 */
enum TestUser {

    USER("user", "secret", Role.USER),
    DEPARTMENT_HEAD("departmentHead", "secret", Role.USER, Role.DEPARTMENT_HEAD),
    SECOND_STAGE_AUTHORITY("secondStageAuthority", "secret", Role.USER, Role.SECOND_STAGE_AUTHORITY),
    BOSS("boss", "secret", Role.USER, Role.BOSS),
    OFFICE("office", "secret", Role.USER, Role.BOSS, Role.OFFICE),
    ADMIN("admin", "secret", Role.USER, Role.ADMIN);

    private final String login;
    private final String password;
    private final Role[] roles;

    TestUser(String login, String password, Role... roles) {

        this.login = login;
        this.password = password;
        this.roles = roles;
    }

    String getLogin() {

        return login;
    }

    Role[] getRoles() {

        return roles;
    }

    String getPassword() {
        return password;
    }
}
