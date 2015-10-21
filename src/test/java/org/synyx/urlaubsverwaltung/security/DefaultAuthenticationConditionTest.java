package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DefaultAuthenticationConditionTest {

    private static final String AUTH_PROPERTY = "auth";

    @Test
    public void ensureConditionDoesNotMatchIfNoAuthenticationSet() {

        System.getProperties().remove(AUTH_PROPERTY);

        DefaultAuthenticationCondition condition = new DefaultAuthenticationCondition();

        Assert.assertFalse("Condition should not match for " + AUTH_PROPERTY + "=<null>",
            condition.matches(null, null));
    }


    @Test
    public void ensureConditionMatchesIfAuthenticationIsSetToDefault() {

        Consumer<String> assertDoesMatch = (auth) -> {
            System.getProperties().put(AUTH_PROPERTY, auth);

            DefaultAuthenticationCondition condition = new DefaultAuthenticationCondition();

            Assert.assertTrue("Condition should match for " + AUTH_PROPERTY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesMatch.accept("default");
        assertDoesMatch.accept("dEfAuLt");
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherAuthentication() {

        Consumer<String> assertDoesNotMatch = (auth) -> {
            System.getProperties().put(AUTH_PROPERTY, auth);

            DefaultAuthenticationCondition condition = new DefaultAuthenticationCondition();

            Assert.assertFalse("Condition should not match for " + AUTH_PROPERTY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesNotMatch.accept("ldap");
        assertDoesNotMatch.accept("activeDirectory");
        assertDoesNotMatch.accept("foo");
    }
}
