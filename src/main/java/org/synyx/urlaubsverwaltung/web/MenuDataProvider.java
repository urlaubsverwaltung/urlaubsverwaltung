package org.synyx.urlaubsverwaltung.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Interceptor to add menu specific information to all requests
 */
@Component
public class MenuDataProvider implements HandlerInterceptor {

    private final PersonService personService;
    private final SettingsService settingsService;
    private final MenuProperties menuProperties;

    @Autowired
    public MenuDataProvider(PersonService personService, SettingsService settingsService, MenuProperties menuProperties) {
        this.personService = personService;
        this.settingsService = settingsService;
        this.menuProperties = menuProperties;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {

        if (modelAndView != null && menuIsShown(modelAndView)) {

            final Person signedInUserInModel = (Person) modelAndView.getModelMap().get("signedInUser");
            final Person user = Objects.requireNonNullElseGet(signedInUserInModel, personService::getSignedInUser);
            final String gravatarUrl = user.getGravatarURL();

            modelAndView.addObject("userFirstName", user.getFirstName());
            modelAndView.addObject("userLastName", user.getLastName());
            modelAndView.addObject("userId", user.getId());
            modelAndView.addObject("menuGravatarUrl", gravatarUrl);
            modelAndView.addObject("menuHelpUrl", menuProperties.getHelp().getUrl());
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled(user));
            modelAndView.addObject("navigationOvertimeItemEnabled", overtimeEnabled(user));

            modelAndView.addObject("isBoss", user.hasRole(BOSS));
            modelAndView.addObject("isOffice", user.hasRole(OFFICE));
            modelAndView.addObject("isDepartmentHead", user.hasRole(DEPARTMENT_HEAD));
            modelAndView.addObject("isSecondStageAuthority", user.hasRole(SECOND_STAGE_AUTHORITY));
        }
    }

    private boolean menuIsShown(ModelAndView modelAndView) {

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("redirect:") && !viewName.startsWith("login");
    }

    private boolean popupMenuEnabled(Person signedInUser) {
        return signedInUser.hasRole(OFFICE) || overtimeEnabled(signedInUser);
    }

    private boolean overtimeEnabled(Person signedInUser) {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        boolean userIsAllowedToWriteOvertime = !overtimeSettings.isOvertimeWritePrivilegedOnly() || signedInUser.isPrivileged();
        return overtimeSettings.isOvertimeActive() && userIsAllowedToWriteOvertime;
    }
}
