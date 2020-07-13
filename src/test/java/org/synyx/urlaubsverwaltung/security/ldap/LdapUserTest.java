package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


class LdapUserTest {

    @Test
    void ensureCanBeInitializedWithEmptyAttributes() {

        LdapUser ldapUser = new LdapUser("username", Optional.empty(), Optional.empty(),
            Optional.empty());

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());
        Assert.assertEquals("First name should be empty", Optional.<String>empty(), ldapUser.getFirstName());
        Assert.assertEquals("Last name should be empty", Optional.<String>empty(), ldapUser.getLastName());
        Assert.assertEquals("Email should be empty", Optional.<String>empty(), ldapUser.getEmail());
    }


    @Test
    void ensureCanBeInitializedWithAttributes() {

        LdapUser ldapUser = new LdapUser("username", Optional.of("Max"), Optional.of("Mustermann"),
            Optional.of("max@firma.test"));

        Assert.assertEquals("Wrong username", "username", ldapUser.getUsername());

        BiConsumer<Optional<String>, String> assertIsSet = (optional, value) -> {
            Assert.assertTrue("Should be set", optional.isPresent());
            Assert.assertEquals("Wrong value", value, optional.get());
        };

        assertIsSet.accept(ldapUser.getFirstName(), "Max");
        assertIsSet.accept(ldapUser.getLastName(), "Mustermann");
        assertIsSet.accept(ldapUser.getEmail(), "max@firma.test");
    }


    @Test
    void ensureMemberOfInformationIsOptional() {

        LdapUser ldapUser = new LdapUser("username", Optional.of("Max"), Optional.of("Mustermann"),
            Optional.of("max@firma.test"));

        List<String> memberOf = ldapUser.getMemberOf();

        Assert.assertNotNull("Should not be null", memberOf);
        Assert.assertTrue("Should be empty", memberOf.isEmpty());
    }


    @Test
    void ensureCanBeInitializedWithAttributesAndMemberOfInformation() {

        LdapUser ldapUser = new LdapUser("username", Optional.of("Max"), Optional.of("Mustermann"),
            Optional.of("max@firma.test"), "GroupA", "GroupB");

        List<String> memberOf = ldapUser.getMemberOf();

        Assert.assertNotNull("Should not be null", memberOf);
        Assert.assertEquals("Wrong number of memberOf elements", 2, memberOf.size());
        Assert.assertTrue("Missing memberOf element", memberOf.contains("GroupA"));
        Assert.assertTrue("Missing memberOf element", memberOf.contains("GroupB"));
    }


    @Test
    void ensureMemberOfListIsUnmodifiable() {

        LdapUser ldapUser = new LdapUser("username", Optional.of("Max"), Optional.of("Mustermann"),
            Optional.of("max@firma.test"), "GroupA", "GroupB");

        List<String> memberOf = ldapUser.getMemberOf();

        Assert.assertNotNull("Should not be null", memberOf);

        try {
            memberOf.add("Foo");
            Assert.fail("List should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @Test
    void ensureThrowsIfInitializedWithEmptyUsername() {

        Consumer<String> assertThrowsOnEmptyUsername = (username) -> {
            try {
                new LdapUser(username, Optional.empty(), Optional.empty(), Optional.empty());
                Assert.fail("Should throw on empty username!");
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertThrowsOnEmptyUsername.accept(null);
        assertThrowsOnEmptyUsername.accept("");
    }
}
