package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;


/**
 * Represents a LDAP user with relevant information.
 */
final class LdapUser {

    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<String> memberOf;

    LdapUser(String username, String firstName, String lastName, String email, List<String> memberOf) {

        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username must be given.");
        }

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.memberOf = memberOf;
    }

    String getUsername() {
        return username;
    }

    Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    Optional<String> getEmail() {

        return Optional.ofNullable(email);
    }

    @Override
    public String toString() {
        return "LdapUser{" +
            "username='" + username + '\'' +
            '}';
    }

    List<String> getMemberOf() {
        return List.copyOf(memberOf);
    }
}
