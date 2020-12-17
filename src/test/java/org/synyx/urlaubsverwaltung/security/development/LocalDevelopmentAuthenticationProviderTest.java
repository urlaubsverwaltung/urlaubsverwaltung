package org.synyx.urlaubsverwaltung.security.development;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class LocalDevelopmentAuthenticationProviderTest {

    private LocalDevelopmentAuthenticationProvider sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new LocalDevelopmentAuthenticationProvider(personService);
    }

    @Test
    void ensureThatValidUserGetsAccess() {

        final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(USER.name()));
        grantedAuthorities.add(new SimpleGrantedAuthority(OFFICE.name()));

        final String username = "user";
        final  Person user = TestDataCreator.createPerson(username, USER, OFFICE);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        final Authentication credentials = new UsernamePasswordAuthenticationToken(username, "", null);
        final Authentication authentication = sut.authenticate(credentials);
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(username);
        assertThat(authentication.getAuthorities()).isEqualTo(grantedAuthorities);
    }

    @Test
    void ensureExceptionIsThrownIfUserCanNotBeFoundWithinDatabase() {

        when(personService.getPersonByUsername(anyString())).thenReturn(Optional.empty());

        final Authentication credentials = new UsernamePasswordAuthenticationToken("user", "", null);
        assertThatThrownBy(() -> sut.authenticate(credentials))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void ensureExceptionIsThrownIfUserIsDeactivated() {

        final String username = "user";
        final Person user = TestDataCreator.createPerson(username, INACTIVE);

        when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

        final Authentication credentials = new UsernamePasswordAuthenticationToken(username, "", null);
        assertThatThrownBy(() -> sut.authenticate(credentials))
            .isInstanceOf(DisabledException.class);
    }
}
