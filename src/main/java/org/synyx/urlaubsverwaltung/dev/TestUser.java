package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.core.person.Role;


/**
 * Test users that can be used to sign in with when test data is created.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum TestUser {

    USER("testUser"),
    DEPARTMENT_HEAD("testHead"),
    BOSS("testBoss"),
    OFFICE("test");

    private String login;

    TestUser(String login) {

        this.login = login;
    }

    public String getLogin() {

        return login;
    }


    /**
     * Get the roles of the test user.
     *
     * @return  array of roles of the test user
     */
    public Role[] getRoles() {

        switch (this) {
            case DEPARTMENT_HEAD:
                return new Role[] { Role.USER, Role.DEPARTMENT_HEAD };

            case BOSS:
                return new Role[] { Role.USER, Role.BOSS };

            case OFFICE:
                return new Role[] { Role.USER, Role.BOSS, Role.OFFICE };

            default:
                return new Role[] { Role.USER };
        }
    }
}
