package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ActiveDirectoryAuthenticationConditionTest {

    @Test
    public void ensureConditionDoesNotMatchIfNoAuthenticationSet() {

        System.getProperties().remove(Authentication.PROPERTY_KEY);

        ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

        Assert.assertFalse("Condition should not match for " + Authentication.PROPERTY_KEY + "=<null>",
            condition.matches(null, null));
    }


    @Test
    public void ensureConditionMatchesIfAuthenticationIsSetToActiveDirectory() {

        Consumer<String> assertDoesMatch = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

            Assert.assertTrue("Condition should match for " + Authentication.PROPERTY_KEY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesMatch.accept(Authentication.Type.ACTIVE_DIRECTORY.getName());
        assertDoesMatch.accept(Authentication.Type.ACTIVE_DIRECTORY.getName().toUpperCase());
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherAuthentication() {

        Consumer<String> assertDoesNotMatch = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            ActiveDirectoryAuthenticationCondition condition = new ActiveDirectoryAuthenticationCondition();

            Assert.assertFalse("Condition should not match for " + Authentication.PROPERTY_KEY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesNotMatch.accept(Authentication.Type.DEFAULT.getName());
        assertDoesNotMatch.accept(Authentication.Type.DEFAULT.getName());
        assertDoesNotMatch.accept("foo");
    }
}
