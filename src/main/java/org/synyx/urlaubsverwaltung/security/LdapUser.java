package org.synyx.urlaubsverwaltung.security;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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

    private Set<String> memberOf = new HashSet<>();

    public LdapUser(String username, Optional<String> firstName, Optional<String> lastName, Optional<String> email,
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


    public List<String> getMemberOf() {

        return Collections.unmodifiableList(memberOf.stream().collect(Collectors.toList()));
    }
}
