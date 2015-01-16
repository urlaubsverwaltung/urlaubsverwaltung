package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.util.Arrays;
import java.util.Collection;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.security.DevUserDetailsService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DevUserDetailsServiceTest {

    private DevUserDetailsService devUserDetailsService;

    private PersonService personService;

    @Before
    public void setUp() {

        personService = Mockito.mock(PersonService.class);
        devUserDetailsService = new DevUserDetailsService(personService);
    }


    @Test(expected = UsernameNotFoundException.class)
    public void ensureThrowsIfTheGivenUserNameDoesNotMatchTestUserName() {

        devUserDetailsService.loadUserByUsername("foo");
    }


    @Test(expected = UsernameNotFoundException.class)
    public void ensureThrowsIfTheGivenUserNameDoesNotMatchTestUserNameIgnoringCase() {

        devUserDetailsService.loadUserByUsername(DevUserDetailsService.TEST_USER_NAME.toUpperCase());
    }


    @Test
    public void ensureReturnsNullIfUserCanNotBeFoundWithinDatabase() {

        String login = DevUserDetailsService.TEST_USER_NAME;

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(null);

        UserDetails userDetails = devUserDetailsService.loadUserByUsername(login);

        Assert.assertNull("Can not return UserDetails if there is no user with given login in database", userDetails);

        Mockito.verify(personService).getPersonByLogin(login);
    }


    @Test
    public void ensureReturnsUserDetailsWithCorrectAuthorities() {

        String login = DevUserDetailsService.TEST_USER_NAME;

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(user);

        UserDetails userDetails = devUserDetailsService.loadUserByUsername(login);

        Assert.assertNotNull("UserDetails should not be null", userDetails);

        Assert.assertEquals("Wrong username", DevUserDetailsService.TEST_USER_NAME, userDetails.getUsername());
        Assert.assertEquals("Wrong password", DevUserDetailsService.TEST_USER_PASSWORD, userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found", authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for office role found", authorityForRoleExists(authorities, Role.OFFICE));
    }


    private boolean authorityForRoleExists(Collection<? extends GrantedAuthority> authorities, final Role role) {

        Optional<? extends GrantedAuthority> authorityForRoleExistsOptional = Iterables.tryFind(authorities,
                new Predicate<GrantedAuthority>() {

                    @Override
                    public boolean apply(GrantedAuthority input) {

                        if (input.getAuthority().equals(role.name())) {
                            return true;
                        }

                        return false;
                    }
                });

        return authorityForRoleExistsOptional.isPresent();
    }
}
