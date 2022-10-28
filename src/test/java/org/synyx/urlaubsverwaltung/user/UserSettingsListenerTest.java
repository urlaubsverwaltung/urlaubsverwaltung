package org.synyx.urlaubsverwaltung.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsListenerTest {
    @Mock
    private UserSettingsService userSettingsService;
    @Mock
    private LocaleResolver localeResolver;

    static Stream<Arguments> authentications() {
        return Stream.of(
            Arguments.of("username", new TestingAuthenticationToken(new User("username", "password", List.of()), null), GERMAN),
            Arguments.of("username", new TestingAuthenticationToken(new User("username", "password", List.of()), null), ENGLISH),
            Arguments.of("username", new TestingAuthenticationToken(new DefaultOidcUser(List.of(), new OidcIdToken("tokenValue", Instant.parse("2020-12-01T00:00:00.00Z"), Instant.parse("2020-12-02T00:00:00.00Z"), Map.of("sub", "username"))), null), GERMAN),
            Arguments.of("username", new TestingAuthenticationToken(new DefaultOidcUser(List.of(), new OidcIdToken("tokenValue", Instant.parse("2020-12-01T00:00:00.00Z"), Instant.parse("2020-12-02T00:00:00.00Z"), Map.of("sub", "username"))), null), ENGLISH)
        );
    }

    @ParameterizedTest
    @MethodSource("authentications")
    void ensureAuthenticationSuccessSetsLocaleForAuthentication(String username, Authentication authentication, Locale locale) {
        final UserSettingsListener sut = new UserSettingsListener(userSettingsService, localeResolver);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userSettingsService.findLocaleForUsername(username)).thenReturn(Optional.of(locale));

        final AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(authentication);
        sut.handleAuthenticationSuccess(authenticationSuccessEvent);

        verify(localeResolver).setLocale(request, null, locale);
    }

    @Test
    void ensureAuthenticationSuccessDoesNotSetLocaleWhenNotAvailable() {
        final UserSettingsListener sut = new UserSettingsListener(userSettingsService, localeResolver);

        when(userSettingsService.findLocaleForUsername("username")).thenReturn(Optional.empty());

        final AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(new TestingAuthenticationToken(new User("username", "password", List.of()), null));
        sut.handleAuthenticationSuccess(authenticationSuccessEvent);

        verifyNoInteractions(localeResolver);
    }

    static Stream<Arguments> locales() {
        return Stream.of(
            Arguments.of(GERMAN),
            Arguments.of(ENGLISH)
        );
    }

    @ParameterizedTest
    @MethodSource("locales")
    void ensureUserLocaleChangedSetsLocaleForAuthentication(Locale locale) {
        final UserSettingsListener sut = new UserSettingsListener(userSettingsService, localeResolver);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        final UserLocaleChangedEvent userLocaleChangedEvent = new UserLocaleChangedEvent(locale);
        sut.handleUserLocaleChanged(userLocaleChangedEvent);

        verify(localeResolver).setLocale(request, null, locale);
    }

    @Test
    void ensureUserLocaleChangedSetsNoLocaleIfRequestIsNotAvailable() {
        final UserSettingsListener sut = new UserSettingsListener(userSettingsService, localeResolver);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(null);

        final UserLocaleChangedEvent userLocaleChangedEvent = new UserLocaleChangedEvent(GERMAN);
        sut.handleUserLocaleChanged(userLocaleChangedEvent);

        verifyNoInteractions(localeResolver);
    }
}
