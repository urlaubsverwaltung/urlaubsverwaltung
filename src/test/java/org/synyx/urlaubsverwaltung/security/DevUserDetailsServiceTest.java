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
import java.util.Optional;


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
    public void ensureThatThrowsExceptionIfUserCanNotBeFoundWithinDatabase() {

        String login = "user";

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(Optional.<Person>empty());

        devUserDetailsService.loadUserByUsername(login);
    }


    @Test
    public void ensureReturnsUserDetailsWithCorrectAuthorities() {

        String login = "user";
        String password = "password";

        Person user = new Person(login, "Max", "Muster", "max.muster@test.de");
        user.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));
        user.setPassword(password);

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(Optional.of(user));

        UserDetails userDetails = devUserDetailsService.loadUserByUsername(login);

        Mockito.verify(personService).getPersonByLogin(login);

        Assert.assertNotNull("UserDetails should not be null", userDetails);

        Assert.assertEquals("Wrong username", login, userDetails.getUsername());
        Assert.assertEquals("Wrong password", password, userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for office role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.OFFICE));
    }
}
