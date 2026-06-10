package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

import java.security.Principal;

@Component
public class UserThemeDataProvider implements DataProviderInterface {

    private final UserSettingsService userSettingsService;

    UserThemeDataProvider(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {

            final Principal userPrincipal = request.getUserPrincipal();

            final UserSettings userSettings = userPrincipal == null
                ? UserSettings.DEFAULT
                : userSettingsService.getUserSettingsForUsername(userPrincipal.getName());

            modelAndView.addObject("theme", userSettings.theme().name().toLowerCase());
            modelAndView.addObject("navigationCollapsed", userSettings.navigationCollapsed());
        }
    }
}
