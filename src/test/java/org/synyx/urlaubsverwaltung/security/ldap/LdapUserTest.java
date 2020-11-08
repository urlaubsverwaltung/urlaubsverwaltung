package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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
        assertThatThrownBy(() -> memberOf.add("Foo")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ensureThrowsIfInitializedWithEmptyUsername() {

        final List<String> emptyList = List.of();

        Consumer<String> assertThrowsOnEmptyUsername = (username) -> {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new LdapUser(username, null, null, null, emptyList));
        };

        assertThrowsOnEmptyUsername.accept(null);
        assertThrowsOnEmptyUsername.accept("");
    }
}
