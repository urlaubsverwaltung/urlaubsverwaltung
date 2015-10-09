package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


/**
 * Provides possibility to login with the system's test user - but only if this test user is persisted in the database.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DevUserDetailsService implements UserDetailsService {

    private final PersonService personService;

    public DevUserDetailsService(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        Optional<Person> userOptional = personService.getPersonByLogin(username);

        if (userOptional.isPresent()) {
            Person person = userOptional.get();

            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            for (final Role role : person.getPermissions()) {
                grantedAuthorities.add(role::name);
            }

            return new User(person.getLoginName(), person.getPassword(), grantedAuthorities);
        } else {
            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }
    }
}
