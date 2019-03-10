package org.synyx.urlaubsverwaltung.security;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SimpleAuthenticationProviderTest {

    private PersonService personService;
    private SimpleAuthenticationProvider authenticationProvider;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);
        authenticationProvider = new SimpleAuthenticationProvider(personService);
    }


    @Test
    public void ensureThatValidUserGetsAccess() {

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Role.USER.name()));
        grantedAuthorities.add(new SimpleGrantedAuthority(Role.OFFICE.name()));

        String username = "user";
        String rawPassword = "secret";
        String encodedPassword = CryptoUtil.encodePassword(rawPassword);

        Person user = TestDataCreator.createPerson(username, Role.USER, Role.OFFICE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByLogin(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, rawPassword, null);
        Authentication authentication = authenticationProvider.authenticate(credentials);

        verify(personService).getPersonByLogin(username);

        Assert.assertNotNull("Missing authentication", authentication);
        Assert.assertEquals("Wrong username", username, authentication.getName());
        Assert.assertEquals("Wrong authorities", grantedAuthorities, authentication.getAuthorities());
    }


    @Test(expected = UsernameNotFoundException.class)
    public void ensureExceptionIsThrownIfUserCanNotBeFoundWithinDatabase() {

        when(personService.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        Authentication credentials = new UsernamePasswordAuthenticationToken("user", "password", null);
        authenticationProvider.authenticate(credentials);
    }


    @Test(expected = DisabledException.class)
    public void ensureExceptionIsThrownIfUserIsDeactivated() {

        String username = "user";
        String rawPassword = "secret";
        String encodedPassword = CryptoUtil.encodePassword(rawPassword);

        Person user = TestDataCreator.createPerson(username, Role.INACTIVE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByLogin(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, rawPassword, null);
        authenticationProvider.authenticate(credentials);
    }


    @Test(expected = AuthenticationException.class)
    public void ensureExceptionIsThrownIfPasswordIsInvalid() {

        String username = "user";
        String encodedPassword = CryptoUtil.encodePassword("secret");

        Person user = TestDataCreator.createPerson(username, Role.USER, Role.OFFICE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByLogin(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, "invalid", null);
        authenticationProvider.authenticate(credentials);
    }
}
