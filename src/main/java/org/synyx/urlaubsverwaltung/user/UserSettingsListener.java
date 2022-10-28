package org.synyx.urlaubsverwaltung.user;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Component
class UserSettingsListener {

    private final UserSettingsService userSettingsService;
    private final LocaleResolver localeResolver;

    UserSettingsListener(UserSettingsService userSettingsService, LocaleResolver localeResolver) {
        this.userSettingsService = userSettingsService;
        this.localeResolver = localeResolver;
    }

    @EventListener
    void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final Authentication authentication = event.getAuthentication();
        final String userName = userName(authentication);

        userSettingsService.findLocaleForUsername(userName).ifPresent(this::setLocale);
    }

    @EventListener
    void handleUserLocaleChanged(UserLocaleChangedEvent event) {
        setLocale(event.getLocale());
    }

    private void setLocale(Locale locale) {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            localeResolver.setLocale(request, null, locale);
        }
    }

    private static String userName(Authentication authentication) {
        String username = null;
        final Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.ldap.userdetails.Person) {
            username = ((org.springframework.security.ldap.userdetails.Person) principal).getUsername();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else if (principal instanceof DefaultOidcUser) {
            username = ((DefaultOidcUser) principal).getIdToken().getSubject();
        }
        return username;
    }
}
