package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import java.util.Locale;
import java.util.Optional;

/**
 * Event listener that listens to successful authentication events of a person
 * and maintains the locale of the person in the user settings as well as
 * in the browser.
 */
@Service
@ConditionalOnSingleTenantMode
class UserSettingsEventListener {

    private final LocaleResolver localeResolver;
    private final PersonService personService;
    private final UserSettingsService userSettingsService;

    UserSettingsEventListener(
        LocaleResolver localeResolver,
        PersonService personService,
        UserSettingsService userSettingsService
    ) {
        this.localeResolver = localeResolver;
        this.personService = personService;
        this.userSettingsService = userSettingsService;
    }

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final String userName = event.getAuthentication().getName();

        personService.getPersonByUsername(userName).ifPresent(person -> {
            updateUserSettingsWithLocaleBrowserSpecific(person);
            userSettingsService.getLocale(person).ifPresent(this::setLocale);
        });
    }

    private void updateUserSettingsWithLocaleBrowserSpecific(Person person) {
        getRequest()
            .map(ServletRequest::getLocale)
            .ifPresent(locale -> userSettingsService.updateLocaleBrowserSpecific(person, locale));
    }

    private void setLocale(Locale locale) {
        getRequest().ifPresent(request -> localeResolver.setLocale(request, null, locale));
    }

    private Optional<HttpServletRequest> getRequest() {
        HttpServletRequest request = null;

        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        return Optional.ofNullable(request);
    }
}
