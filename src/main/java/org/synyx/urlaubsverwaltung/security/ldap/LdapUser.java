package org.synyx.urlaubsverwaltung.security.ldap;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Represents a LDAP user with relevant information.
 */
final class LdapUser {

    private final String username;

    private String firstName;
    private String lastName;
    private String email;

    private final Set<String> memberOf = new HashSet<>();

    LdapUser(String username, Optional<String> firstName, Optional<String> lastName, Optional<String> email,
             String... memberOf) {

        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username must be given.");
        }

        this.username = username;

        if (firstName.isPresent()) {
            this.firstName = firstName.get();
        }

        if (lastName.isPresent()) {
            this.lastName = lastName.get();
        }

        if (email.isPresent()) {
            this.email = email.get();
        }

        Collections.addAll(this.memberOf, memberOf);
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

        return Collections.unmodifiableList(new ArrayList<>(memberOf));
    }
}
