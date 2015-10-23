package org.synyx.urlaubsverwaltung.security;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.util.StringUtils;

import java.util.Optional;


/**
 * Represents a LDAP user with relevant information.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public final class LdapUser {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public LdapUser(String username, Optional<String> firstName, Optional<String> lastName, Optional<String> email) {

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
    }

    public String getUsername() {

        return username;
    }


    public Optional<String> getFirstName() {

        return Optional.ofNullable(firstName);
    }


    public Optional<String> getLastName() {

        return Optional.ofNullable(lastName);
    }


    public Optional<String> getEmail() {

        return Optional.ofNullable(email);
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
