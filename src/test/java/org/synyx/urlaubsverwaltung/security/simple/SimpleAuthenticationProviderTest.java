package org.synyx.urlaubsverwaltung.security.simple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SimpleAuthenticationProviderTest {

    private PersonService personService;
    private SimpleAuthenticationProvider authenticationProvider;
    private PasswordEncoder passwordEncoder;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);
        passwordEncoder = new CustomPasswordEncoder();
        authenticationProvider = new SimpleAuthenticationProvider(personService, passwordEncoder);
    }


    @Test
    public void ensureThatValidUserGetsAccess() {

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Role.USER.name()));
        grantedAuthorities.add(new SimpleGrantedAuthority(Role.OFFICE.name()));

        String username = "user";
        String rawPassword = "secret";
        String encodedPassword = "2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec";

        Person user = TestDataCreator.createPerson(username, Role.USER, Role.OFFICE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, rawPassword, null);
        Authentication authentication = authenticationProvider.authenticate(credentials);

        verify(personService).getPersonByUsername(username);

        Assert.assertNotNull("Missing authentication", authentication);
        Assert.assertEquals("Wrong username", username, authentication.getName());
        Assert.assertEquals("Wrong authorities", grantedAuthorities, authentication.getAuthorities());
    }


    @Test(expected = UsernameNotFoundException.class)
    public void ensureExceptionIsThrownIfUserCanNotBeFoundWithinDatabase() {

        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.empty());

        Authentication credentials = new UsernamePasswordAuthenticationToken("user", "password", null);
        authenticationProvider.authenticate(credentials);
    }


    @Test(expected = DisabledException.class)
    public void ensureExceptionIsThrownIfUserIsDeactivated() {

        String username = "user";
        String rawPassword = "secret";
        String encodedPassword = "2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec";

        Person user = TestDataCreator.createPerson(username, Role.INACTIVE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, rawPassword, null);
        authenticationProvider.authenticate(credentials);
    }


    @Test(expected = AuthenticationException.class)
    public void ensureExceptionIsThrownIfPasswordIsInvalid() {

        String username = "user";
        String encodedPassword = "2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec";

        Person user = TestDataCreator.createPerson(username, Role.USER, Role.OFFICE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, "invalid", null);
        authenticationProvider.authenticate(credentials);
    }
}
