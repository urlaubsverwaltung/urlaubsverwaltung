package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ActiveDirectoryAuthenticationConditionTest {

    private static final String AUTH_PROPERTY = "auth";

    @Test
    public void ensureConditionDoesNotMatchIfNoAuthenticationSet() {

        System.getProperties().remove(AUTH_PROPERTY);

        ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

        Assert.assertFalse("Condition should not match for " + AUTH_PROPERTY + "=<null>",
            condition.matches(null, null));
    }


    @Test
    public void ensureConditionMatchesIfAuthenticationIsSetToActiveDirectory() {

        Consumer<String> assertDoesMatch = (auth) -> {
            System.getProperties().put(AUTH_PROPERTY, auth);

            ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

            Assert.assertTrue("Condition should match for " + AUTH_PROPERTY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesMatch.accept("activeDirectory");
        assertDoesMatch.accept("activedirectory");
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherAuthentication() {

        Consumer<String> assertDoesNotMatch = (auth) -> {
            System.getProperties().put(AUTH_PROPERTY, auth);

            ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

            Assert.assertFalse("Condition should not match for " + AUTH_PROPERTY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesNotMatch.accept("default");
        assertDoesNotMatch.accept("ldap");
        assertDoesNotMatch.accept("foo");
    }
}
