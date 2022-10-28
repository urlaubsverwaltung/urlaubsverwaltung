package org.synyx.urlaubsverwaltung.security.simple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleAuthenticationProviderTest {

    private SimpleAuthenticationProvider sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new SimpleAuthenticationProvider(personService, new CustomPasswordEncoder());
    }

    @Test
    void ensureThatValidUserGetsAccess() {

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
        Authentication authentication = sut.authenticate(credentials);

        verify(personService).getPersonByUsername(username);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getAuthorities()).isEqualTo(grantedAuthorities);
    }


    @Test
    void ensureExceptionIsThrownIfUserCanNotBeFoundWithinDatabase() {

        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.empty());

        Authentication credentials = new UsernamePasswordAuthenticationToken("user", "password", null);
        assertThatThrownBy(() -> sut.authenticate(credentials))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void ensureExceptionIsThrownIfUserIsDeactivated() {

        String username = "user";
        String rawPassword = "secret";
        String encodedPassword = "2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec";

        Person user = TestDataCreator.createPerson(username, Role.INACTIVE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, rawPassword, null);
        assertThatThrownBy(() -> sut.authenticate(credentials))
            .isInstanceOf(DisabledException.class);
    }

    @Test
    void ensureExceptionIsThrownIfPasswordIsInvalid() {

        String username = "user";
        String encodedPassword = "2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec";

        Person user = TestDataCreator.createPerson(username, Role.USER, Role.OFFICE);
        user.setPassword(encodedPassword);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        Authentication credentials = new UsernamePasswordAuthenticationToken(username, "invalid", null);
        assertThatThrownBy(() -> sut.authenticate(credentials))
            .isInstanceOf(AuthenticationException.class);
    }
}
