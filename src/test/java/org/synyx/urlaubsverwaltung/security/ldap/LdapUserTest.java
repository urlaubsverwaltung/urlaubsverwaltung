package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LdapUserTest {

    @Test
    void ensureCanBeInitializedWithAttributes() {
        final LdapUser ldapUser = new LdapUser("username", "Max", "Mustermann", "max@example.org", List.of());
        assertThat(ldapUser.getUsername()).isEqualTo("username");
        assertThat(ldapUser.getFirstName()).isEqualTo("Max");
        assertThat(ldapUser.getLastName()).isEqualTo("Mustermann");
        assertThat(ldapUser.getEmail()).isEqualTo("max@example.org");
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
        assertThatThrownBy(() -> memberOf.add("Foo")).isInstanceOf(UnsupportedOperationException.class);
    }
}
