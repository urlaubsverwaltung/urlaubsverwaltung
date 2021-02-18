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

@Component
class NavigationInterceptor implements HandlerInterceptor {

    private final SettingsService settingsService;
    private final PersonService personService;

    @Autowired
    NavigationInterceptor(SettingsService settingsService, PersonService personService) {
        this.settingsService = settingsService;
        this.personService = personService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled());
            modelAndView.addObject("navigationOvertimeItemEnabled", overtimeEnabled());
        }
    }

    private boolean popupMenuEnabled() {
        boolean office = false;
        try {
            final Person signedInUser = personService.getSignedInUser();
            office = signedInUser.hasRole(Role.OFFICE);
        } catch (Exception ignore) {
            // e.g. no signed in user available
        }
        return office || overtimeEnabled();
    }

    private boolean overtimeEnabled() {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        return overtimeSettings.isOvertimeActive();
    }
}
