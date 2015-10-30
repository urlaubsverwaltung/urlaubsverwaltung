package org.synyx.urlaubsverwaltung.security;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

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
@Service("userDetailsService")
@ConditionalOnProperty(name = "auth", havingValue = "default")
public class SimpleUserDetailsService implements UserDetailsService {

    private static final Logger LOG = Logger.getLogger(SimpleUserDetailsService.class);

    private final PersonService personService;

    @Autowired
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

            Collection<Role> permissions = person.getPermissions();
            permissions.stream().forEach(role -> grantedAuthorities.add(role::name));

            LOG.info("User '" + username + "' has signed in with roles: " + permissions);

            return new User(person.getLoginName(), person.getPassword(), grantedAuthorities);
        } else {
            LOG.info("No user with username '" + username + "' found");
            throw new UsernameNotFoundException("No authentication possible for user = " + username);
        }
    }
}
