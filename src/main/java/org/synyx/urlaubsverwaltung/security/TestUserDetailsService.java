package org.synyx.urlaubsverwaltung.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonDAO;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Provides possibility to login with the system's test user - but only if this test user is persisted in the database.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class TestUserDetailsService implements UserDetailsService {

    private static final String TEST_USER_NAME = "test";
    private static final String TEST_USER_PASSWORD = "secret";

    private PersonDAO personDAO;

    public TestUserDetailsService(PersonDAO personDAO) {

        this.personDAO = personDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        Person testUser = personDAO.findByLoginName(TEST_USER_NAME);

        if (testUser != null) {
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            grantedAuthorities.add(new GrantedAuthorityImpl(Role.USER.toString()));
            grantedAuthorities.add(new GrantedAuthorityImpl(Role.BOSS.toString()));
            grantedAuthorities.add(new GrantedAuthorityImpl(Role.OFFICE.toString()));

            return new User(TEST_USER_NAME, TEST_USER_PASSWORD, grantedAuthorities);
        }

        return null;
    }
}
