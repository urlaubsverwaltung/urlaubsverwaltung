package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsAwareSessionLocaleResolverTest {

    @Mock
    private UserSettingsService userSettingsService;

    @Test
    void ensureDetermineDefaultLocaleReturnsRequestLocaleWhenPrincipalDoesNotExist() {

        final UserSettingsAwareSessionLocaleResolver sut = new UserSettingsAwareSessionLocaleResolver(userSettingsService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());
        request.setUserPrincipal(null);

        final Locale actual = sut.determineDefaultLocale(request);

        assertThat(actual).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void ensureDetermineDefaultLocaleReturnsRequestLocaleWhenPrincipalHasNoExplicitLocaleSet() {

        when(userSettingsService.findLocaleForUsername("batman")).thenReturn(Optional.empty());

        final UserSettingsAwareSessionLocaleResolver sut = new UserSettingsAwareSessionLocaleResolver(userSettingsService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());
        request.setUserPrincipal(anyPrincipal("batman"));

        final Locale actual = sut.determineDefaultLocale(request);

        assertThat(actual).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void ensureDetermineDefaultLocaleReturnsUserLocale() {

        when(userSettingsService.findLocaleForUsername("batman")).thenReturn(Optional.of(Locale.GERMAN));

        final UserSettingsAwareSessionLocaleResolver sut = new UserSettingsAwareSessionLocaleResolver(userSettingsService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalName(Locale.ENGLISH.getLanguage());
        request.setUserPrincipal(anyPrincipal("batman"));

        final Locale actual = sut.determineDefaultLocale(request);

        assertThat(actual).isEqualTo(Locale.GERMAN);
    }

    private static Principal anyPrincipal(String username) {
        final UserDetails user = User.withUsername(username).password("secret").authorities(new SimpleGrantedAuthority("ROLE_USER")).build();
        return new UsernamePasswordAuthenticationToken(user, null);
    }
}
