package org.synyx.urlaubsverwaltung.user;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Optional;

@Component
public class UserThemeDataProvider implements HandlerInterceptor {

    private final UserSettingsServiceImpl userSettingsService;

    UserThemeDataProvider(UserSettingsServiceImpl userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (themeIsNeeded(modelAndView)) {

            final Principal userPrincipal = request.getUserPrincipal();

            final Theme theme = userPrincipal == null ? Theme.SYSTEM : getTheme(userPrincipal).orElse(Theme.SYSTEM);
            final String themeValueLowerCase = theme.name().toLowerCase();

            modelAndView.addObject("theme", themeValueLowerCase);
        }
    }

    private boolean themeIsNeeded(ModelAndView modelAndView) {

        if (modelAndView == null) {
            return false;
        }

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("forward:") &&
            !viewName.startsWith("redirect:");
    }

    private Optional<Theme> getTheme(Principal principal) {
        final String username = principal.getName();
        return userSettingsService.findThemeForUsername(username);
    }
}
