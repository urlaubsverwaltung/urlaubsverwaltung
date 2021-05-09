package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Interceptor to add menu specific information to all requests
 */
@Component
public class MenuDataProvider implements HandlerInterceptor {

    private final PersonService personService;
    private final SettingsService settingsService;

    @Autowired
    public MenuDataProvider(PersonService personService, SettingsService settingsService) {
        this.personService = personService;
        this.settingsService = settingsService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

        if (menuIsShown(modelAndView)) {

            final Person signedInUserInModel = (Person) modelAndView.getModelMap().get("signedInUser");
            final Person user = Objects.requireNonNullElseGet(signedInUserInModel, personService::getSignedInUser);
            final String gravatarUrl = user.getGravatarURL();

            modelAndView.addObject("userFirstName", user.getFirstName());
            modelAndView.addObject("userLastName", user.getLastName());
            modelAndView.addObject("userId", user.getId());
            modelAndView.addObject("menuGravatarUrl", gravatarUrl);
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled(user));
            modelAndView.addObject("navigationOvertimeItemEnabled", overtimeEnabled());
        }
    }

    private boolean menuIsShown(ModelAndView modelAndView) {

        if (modelAndView == null || modelAndView.getViewName() == null) {
            return false;
        }

        final String viewName = modelAndView.getViewName();
        return !viewName.startsWith("redirect:")
            && !viewName.startsWith("login");
    }

    private boolean popupMenuEnabled(Person signedInUser) {
        return signedInUser.hasRole(Role.OFFICE) || overtimeEnabled();
    }

    private boolean overtimeEnabled() {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        return overtimeSettings.isOvertimeActive();
    }
}
