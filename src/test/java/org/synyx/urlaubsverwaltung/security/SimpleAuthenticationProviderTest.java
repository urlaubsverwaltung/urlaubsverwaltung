package org.synyx.urlaubsverwaltung.security;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public class SimpleAuthenticationProviderTest {

    private SimpleUserDetailsService userDetailsService;

    @Before
    public void setUp() {

        userDetailsService = Mockito.mock(SimpleUserDetailsService.class);
    }


    @Test
    public void ensureThatValidUserGetsAccess() {

        Collection<Role> grantedRoles = Arrays.asList(Role.USER, Role.OFFICE);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (Role role : grantedRoles) {
            grantedAuthorities.add(role::name);
        }

        String rawPassword = "secret";
        String encodedPassword = CryptoUtil.encodePassword(rawPassword);

        User validUser = new User("valid", encodedPassword, grantedAuthorities);

        Mockito.when(userDetailsService.loadUserByUsername("valid")).thenReturn(validUser);

        AuthenticationProvider cot = new SimpleAuthenticationProvider(userDetailsService);
        Authentication authToken = new UsernamePasswordAuthenticationToken(validUser.getUsername(), rawPassword,
                validUser.getAuthorities());
        cot.authenticate(authToken);
        Mockito.verify(userDetailsService).loadUserByUsername("valid");
    }


    @Test(expected = AuthenticationException.class)
    public void ensureThatInvalidPasswordGetsAccessDenied() {

        Collection<Role> grantedRoles = Arrays.asList(Role.USER, Role.OFFICE);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (Role role : grantedRoles) {
            grantedAuthorities.add(role::name);
        }

        String encodedPassword = CryptoUtil.encodePassword("secret");

        User validUser = new User("valid", encodedPassword, grantedAuthorities);
        Mockito.when(userDetailsService.loadUserByUsername("valid")).thenReturn(validUser);

        AuthenticationProvider cot = new SimpleAuthenticationProvider(userDetailsService);
        Authentication authToken = new UsernamePasswordAuthenticationToken(validUser.getUsername(), "foo",
                validUser.getAuthorities());
        cot.authenticate(authToken);
    }


    @Test(expected = AuthenticationException.class)
    public void ensureThatInvalidUserGetsAccessDenied() {

        Collection<Role> grantedRoles = Arrays.asList(Role.USER, Role.OFFICE);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (Role role : grantedRoles) {
            grantedAuthorities.add(role::name);
        }

        AuthenticationProvider cot = new SimpleAuthenticationProvider(userDetailsService);
        Authentication authToken = new UsernamePasswordAuthenticationToken("invalid", "foo", grantedAuthorities);
        cot.authenticate(authToken);
    }
}
