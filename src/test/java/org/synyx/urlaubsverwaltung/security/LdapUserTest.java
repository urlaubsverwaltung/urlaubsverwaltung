package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class LdapUserTest {

    @Test
    public void ensureCanBeInitializedWithEmptyAttributes() {

        LdapUser ldapUser = new LdapUser("username", Optional.<String>empty(), Optional.<String>empty(),
                Optional.<String>empty());

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());
        Assert.assertEquals("First name should be empty", Optional.<String>empty(), ldapUser.getFirstName());
        Assert.assertEquals("Last name should be empty", Optional.<String>empty(), ldapUser.getLastName());
        Assert.assertEquals("Email should be empty", Optional.<String>empty(), ldapUser.getEmail());
    }


    @Test
    public void ensureCanBeInitializedWithAttributes() {

        LdapUser ldapUser = new LdapUser("username", Optional.of("Max"), Optional.of("Mustermann"),
                Optional.of("max@muster.de"));

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());

        BiConsumer<Optional<String>, String> assertIsSet = (optional, value) -> {
            Assert.assertTrue("Should be set", optional.isPresent());
            Assert.assertEquals("Wrong value", value, optional.get());
        };

        assertIsSet.accept(ldapUser.getFirstName(), "Max");
        assertIsSet.accept(ldapUser.getLastName(), "Mustermann");
        assertIsSet.accept(ldapUser.getEmail(), "max@muster.de");
    }


    @Test
    public void ensureThrowsIfInitializedWithEmptyUsername() {

        Consumer<String> assertThrowsOnEmptyUsername = (username) -> {
            try {
                new LdapUser(username, Optional.<String>empty(), Optional.<String>empty(), Optional.<String>empty());
                Assert.fail("Should throw on empty username!");
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertThrowsOnEmptyUsername.accept(null);
        assertThrowsOnEmptyUsername.accept("");
    }
}
