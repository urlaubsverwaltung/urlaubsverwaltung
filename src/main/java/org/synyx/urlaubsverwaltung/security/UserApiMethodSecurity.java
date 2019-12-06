package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

@Component
public class UserApiMethodSecurity {

    private PersonService personService;

    @Autowired
    public UserApiMethodSecurity(PersonService personService) {
        this.personService = personService;
    }

    public boolean isSamePersonId(Authentication authentication, Integer userId) {

        final Optional<Person> person = personService.getPersonByID(userId);
        if (person.isEmpty()) {
            return false;
        }

        boolean allowed = false;
        if (authentication.getPrincipal() instanceof org.springframework.security.ldap.userdetails.Person) {
            final String username = ((org.springframework.security.ldap.userdetails.Person) authentication.getPrincipal()).getUsername();
            allowed = username.equals(person.get().getUsername());
        } else if (authentication.getPrincipal() instanceof User) {
            allowed = ((User) authentication.getPrincipal()).getUsername().equals(person.get().getUsername());
        } else if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            final String username = ((DefaultOidcUser) authentication.getPrincipal()).getIdToken().getSubject();
            allowed = username.equals(person.get().getUsername());
        }

        return allowed;
    }
}
