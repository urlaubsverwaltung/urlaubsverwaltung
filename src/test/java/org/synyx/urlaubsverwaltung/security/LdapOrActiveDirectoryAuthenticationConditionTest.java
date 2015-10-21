package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapOrActiveDirectoryAuthenticationConditionTest {

    @Test
    public void ensureConditionMatchesIfAuthenticationIsSetToLdap() {

        Consumer<String> assertDoesMatch = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            LdapOrActiveDirectoryAuthenticationCondition condition = new LdapOrActiveDirectoryAuthenticationCondition();

            Assert.assertTrue("Condition should match for " + Authentication.PROPERTY_KEY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesMatch.accept(Authentication.Type.LDAP.getName());
        assertDoesMatch.accept(Authentication.Type.LDAP.getName().toUpperCase());
    }


    @Test
    public void ensureConditionMatchesIfAuthenticationIsSetToActiveDirectory() {

        Consumer<String> assertDoesMatch = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            LdapOrActiveDirectoryAuthenticationCondition condition = new LdapOrActiveDirectoryAuthenticationCondition();

            Assert.assertTrue("Condition should match for " + Authentication.PROPERTY_KEY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesMatch.accept(Authentication.Type.ACTIVE_DIRECTORY.getName());
        assertDoesMatch.accept(Authentication.Type.ACTIVE_DIRECTORY.getName().toUpperCase());
    }


    @Test
    public void ensureConditionDoesNotMatchIfNoAuthenticationSet() {

        System.getProperties().remove(Authentication.PROPERTY_KEY);

        LdapOrActiveDirectoryAuthenticationCondition condition = new LdapOrActiveDirectoryAuthenticationCondition();

        Assert.assertFalse("Condition should not match for " + Authentication.PROPERTY_KEY + "=<null>",
            condition.matches(null, null));
    }


    @Test
    public void ensureConditionDoesNotMatchForOtherAuthentication() {

        Consumer<String> assertDoesNotMatch = (auth) -> {
            System.getProperties().put(Authentication.PROPERTY_KEY, auth);

            LdapOrActiveDirectoryAuthenticationCondition condition = new LdapOrActiveDirectoryAuthenticationCondition();

            Assert.assertFalse("Condition should not match for " + Authentication.PROPERTY_KEY + "=" + auth,
                condition.matches(null, null));
        };

        assertDoesNotMatch.accept(Authentication.Type.DEFAULT.getName());
        assertDoesNotMatch.accept("foo");
    }
}
