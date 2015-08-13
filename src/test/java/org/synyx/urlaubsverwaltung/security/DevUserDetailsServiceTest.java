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
import org.synyx.urlaubsverwaltung.core.startup.TestUser;

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

        Mockito.when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.<Person>empty());
    }


    @Test(expected = UsernameNotFoundException.class)
    public void ensureThrowsIfTheGivenUserNameDoesNotMatchOneOfTheTestUserNames() {

        devUserDetailsService.loadUserByUsername("foo");
    }


    @Test
    public void ensureDoesNotThrowForTestUser() {

        String login = TestUser.USER.getLogin();

        try {
            devUserDetailsService.loadUserByUsername(login);
        } catch (UsernameNotFoundException ex) {
            Assert.fail("Should not throw for user with name: " + login);
        }
    }


    @Test
    public void ensureDoesNotThrowForTestDepartmentHead() {

        String login = TestUser.DEPARTMENT_HEAD.getLogin();

        try {
            devUserDetailsService.loadUserByUsername(login);
        } catch (UsernameNotFoundException ex) {
            Assert.fail("Should not throw for user with name: " + login);
        }
    }


    @Test
    public void ensureDoesNotThrowForTestBoss() {

        String login = TestUser.BOSS.getLogin();

        try {
            devUserDetailsService.loadUserByUsername(login);
        } catch (UsernameNotFoundException ex) {
            Assert.fail("Should not throw for user with name: " + login);
        }
    }


    @Test
    public void ensureDoesNotThrowForTestOffice() {

        String login = TestUser.OFFICE.getLogin();

        try {
            devUserDetailsService.loadUserByUsername(login);
        } catch (UsernameNotFoundException ex) {
            Assert.fail("Should not throw for user with name: " + login);
        }
    }


    @Test
    public void ensureReturnsNullIfUserCanNotBeFoundWithinDatabase() {

        String login = TestUser.USER.getLogin();

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(Optional.<Person>empty());

        UserDetails userDetails = devUserDetailsService.loadUserByUsername(login);

        Assert.assertNull("Can not return UserDetails if there is no user with given login in database", userDetails);

        Mockito.verify(personService).getPersonByLogin(login);
    }


    @Test
    public void ensureReturnsUserDetailsWithCorrectAuthorities() {

        String login = TestUser.USER.getLogin();

        Person user = new Person();
        user.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Mockito.when(personService.getPersonByLogin(login)).thenReturn(Optional.of(user));

        UserDetails userDetails = devUserDetailsService.loadUserByUsername(login);

        Assert.assertNotNull("UserDetails should not be null", userDetails);

        Assert.assertEquals("Wrong username", login, userDetails.getUsername());
        Assert.assertEquals("Wrong password", DevUserDetailsService.TEST_USER_PASSWORD, userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for office role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.OFFICE));
    }
}
