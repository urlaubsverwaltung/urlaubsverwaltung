package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


class LdapUserTest {

    @Test
    void ensureCanBeInitializedWithEmptyAttributes() {
        final LdapUser ldapUser = new LdapUser("username", null, null, null, List.of());
        assertThat(ldapUser.getUsername()).isEqualTo("username");
        assertThat(ldapUser.getFirstName()).isEmpty();
        assertThat(ldapUser.getLastName()).isEmpty();
        assertThat(ldapUser.getEmail()).isEmpty();
    }

    @Test
    void ensureCanBeInitializedWithAttributes() {
        final LdapUser ldapUser = new LdapUser("username", "Max", "Mustermann", "max@example.org", List.of());
        assertThat(ldapUser.getUsername()).isEqualTo("username");
        assertThat(ldapUser.getFirstName()).isEqualTo(Optional.of("Max"));
        assertThat(ldapUser.getLastName()).isEqualTo(Optional.of("Mustermann"));
        assertThat(ldapUser.getEmail()).isEqualTo(Optional.of("max@example.org"));
    }

    @Test
    void ensureMemberOfInformationIsOptional() {
        final LdapUser ldapUser = new LdapUser("username", "Max", "Mustermann", "max@example.org", List.of());
        assertThat(ldapUser.getMemberOf()).isEmpty();
    }

    @Test
    void ensureCanBeInitializedWithAttributesAndMemberOfInformation() {
        final LdapUser ldapUser = new LdapUser("username", "Max", "Mustermann", "max@example.org", List.of("GroupA", "GroupB"));
        assertThat(ldapUser.getMemberOf())
            .hasSize(2)
            .contains("GroupA", "GroupB");
    }

    @Test
    void ensureMemberOfListIsUnmodifiable() {

        final LdapUser ldapUser = new LdapUser("username", "Max", "Mustermann", "max@example.org", List.of("GroupA", "GroupB"));

        final List<String> memberOf = ldapUser.getMemberOf();
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
                new LdapUser(username, null, null, null, List.of());
                Assert.fail("Should throw on empty username!");
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertThrowsOnEmptyUsername.accept(null);
        assertThrowsOnEmptyUsername.accept("");
    }
}
