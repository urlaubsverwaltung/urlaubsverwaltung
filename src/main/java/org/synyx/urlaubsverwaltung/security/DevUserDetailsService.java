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


/**
 * Provides possibility to login with the system's test user - but only if this test user is persisted in the database.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DevUserDetailsService implements UserDetailsService {

    static final String TEST_USER_NAME = "test";
    static final String TEST_USER_PASSWORD = "secret";

    private final PersonService personService;

    public DevUserDetailsService(PersonService personService) {

        this.personService = personService;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        if (!s.equals(TEST_USER_NAME)) {
            throw new UsernameNotFoundException("No authentication possible for user with name " + s);
        }

        Person testUser = personService.getPersonByLogin(TEST_USER_NAME);

        if (testUser != null) {
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            Collection<Role> roles = testUser.getPermissions();

            for (final Role role : roles) {
                grantedAuthorities.add(new GrantedAuthority() {

                        @Override
                        public String getAuthority() {

                            return role.name();
                        }
                    });
            }

            return new User(TEST_USER_NAME, TEST_USER_PASSWORD, grantedAuthorities);
        }

        return null;
    }
}
