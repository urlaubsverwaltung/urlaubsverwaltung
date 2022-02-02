package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class UserThemeDataProvider implements HandlerInterceptor {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final UserSettingsService userSettingsService;

    UserThemeDataProvider(PersonService personService, UserSettingsService userSettingsService) {
        this.personService = personService;
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (themeIsNeeded(modelAndView)) {

            final Theme theme = getTheme().orElse(Theme.SYSTEM);
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
            !viewName.startsWith("redirect:") &&
            !viewName.startsWith("login");
    }

    private Optional<Theme> getTheme() {
        try {
            final Person signedInUser = personService.getSignedInUser();
            return Optional.of(userSettingsService.getUserSettingsForPerson(signedInUser).theme());
        } catch (IllegalStateException e) {
            LOG.info("could not get theme for unknown user (login page called for instance).", e);
            return Optional.empty();
        }
    }
}
