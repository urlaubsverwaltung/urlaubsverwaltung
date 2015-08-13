package org.synyx.urlaubsverwaltung.core.startup;

import org.synyx.urlaubsverwaltung.security.Role;


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


    /**
     * Check if there is a test user with the given login.
     *
     * @param  login  to check
     *
     * @return  {@code true} if there is a test user with the given login, else {@code false}
     */
    public static boolean hasUserWithLogin(String login) {

        for (TestUser testUser : TestUser.values()) {
            if (testUser.getLogin().equals(login)) {
                return true;
            }
        }

        return false;
    }
}
