package org.synyx.urlaubsverwaltung.user;

import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Locale;

class UserSettingsAwareSessionLocaleResolver extends SessionLocaleResolver {

    private final UserSettingsService userSettingsService;

    UserSettingsAwareSessionLocaleResolver(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {

        final Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal == null) {
            return request.getLocale();
        }

        final Locale locale = userSettingsService.findLocaleForUsername(userPrincipal.getName())
            .orElseGet(request::getLocale);

        return locale;
    }
}
