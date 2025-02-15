package org.synyx.urlaubsverwaltung.user;


import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Locale.GERMAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsEventListenerTest {

    @InjectMocks
    private UserSettingsEventListener sut;

    @Mock
    private UserSettingsService userSettingsService;
    @Mock
    private LocaleResolver localeResolver;
    @Mock
    private PersonService personService;


    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void ensuresBrowserLocalePresentWillDelegateToUpdateLocaleBrowserSpecific() {

        final Locale locale = GERMAN;
        final Authentication authentication = authToken("username");

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("username");
        when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person));

        // locale is provided via http request -> will be delegated to updateLocaleBrowserSpecific
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(locale);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // already configured locale will be set in the locale resolver
        when(userSettingsService.getLocale(any())).thenReturn(Optional.of(locale));

        final AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(authentication);
        sut.handleAuthenticationSuccess(authenticationSuccessEvent);

        verify(userSettingsService).getLocale(person);

        verify(userSettingsService).updateLocaleBrowserSpecific(person, locale);

        verify(localeResolver).setLocale(request, null, locale);
    }


    @Test
    void ensureWithoutBrowserLocaleNoInteractionWithUpdateLocaleBrowserSpecific() {

        final Locale locale = GERMAN;
        final Authentication authentication = authToken("username");

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("username");
        when(personService.getPersonByUsername("username")).thenReturn(Optional.of(person));

        // locale is missing via http request -> no interaction with updateLocaleBrowserSpecific
        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // already configured locale will be set in the locale resolver
        when(userSettingsService.getLocale(any())).thenReturn(Optional.of(locale));

        final AuthenticationSuccessEvent authenticationSuccessEvent = new AuthenticationSuccessEvent(authentication);
        sut.handleAuthenticationSuccess(authenticationSuccessEvent);

        verify(userSettingsService).getLocale(person);

        verify(userSettingsService, never()).updateLocaleBrowserSpecific(person, locale);

        verify(localeResolver).setLocale(request, null, locale);
    }

    private static @NotNull TestingAuthenticationToken authToken(String username) {
        return new TestingAuthenticationToken(new DefaultOidcUser(List.of(), new OidcIdToken("tokenValue", Instant.parse("2020-12-01T00:00:00.00Z"), Instant.parse("2020-12-02T00:00:00.00Z"), Map.of("sub", username))), null);
    }
}
