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

    static final String TEST_USER_PASSWORD = "secret";

    private final PersonService personService;

    public DevUserDetailsService(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        Optional<Person> userOptional = personService.getPersonByLogin(username);

        if (userOptional != null && userOptional.isPresent()) {
            Person person = userOptional.get();

            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            Collection<Role> roles = person.getPermissions();

            for (final Role role : roles) {
                grantedAuthorities.add(role::name);
            }

            return new User(person.getLoginName(), person.getPassword(), grantedAuthorities);
        } else {
            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }
    }
}
