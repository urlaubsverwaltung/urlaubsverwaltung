package org.synyx.urlaubsverwaltung.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.web.DataProviderInterface;

import java.security.Principal;
import java.util.Optional;

@Component
public class UserThemeDataProvider implements DataProviderInterface {

    private final UserSettingsServiceImpl userSettingsService;

    UserThemeDataProvider(UserSettingsServiceImpl userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (addDataIf(modelAndView)) {

            final Principal userPrincipal = request.getUserPrincipal();

            final Theme theme = userPrincipal == null ? Theme.SYSTEM : getTheme(userPrincipal).orElse(Theme.SYSTEM);
            final String themeValueLowerCase = theme.name().toLowerCase();

            modelAndView.addObject("theme", themeValueLowerCase);
        }
    }

    private Optional<Theme> getTheme(Principal principal) {
        final String username = principal.getName();
        return userSettingsService.findThemeForUsername(username);
    }
}
