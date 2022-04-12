package org.synyx.urlaubsverwaltung.security.ldap;

import java.util.List;


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
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.memberOf = memberOf;
    }

    String getUsername() {
        return username;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    String getEmail() {
        return email;
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
