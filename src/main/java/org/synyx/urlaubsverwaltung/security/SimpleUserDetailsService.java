package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


/**
 * Load user specific data for successfully authenticated user from database.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SimpleUserDetailsService implements UserDetailsService {

    private final PersonService personService;

    public SimpleUserDetailsService(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        // TODO: Think about what to do if no active person exists yet.
        // boolean noActivePersonExistsYet = personService.getActivePersons().isEmpty();

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
