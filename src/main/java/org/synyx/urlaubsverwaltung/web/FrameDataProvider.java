package org.synyx.urlaubsverwaltung.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.ArrayList;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

/**
 * Interceptor to add menu specific information to all requests
 */
@Component
public class FrameDataProvider implements HandlerInterceptor {

    private final PersonService personService;
    private final SettingsService settingsService;
    private final MenuProperties menuProperties;
    private final String applicationVersion;

    @Autowired
    public FrameDataProvider(PersonService personService, SettingsService settingsService, MenuProperties menuProperties,
                             @Value("${info.app.version}") String applicationVersion) {
        this.personService = personService;
        this.settingsService = settingsService;
        this.menuProperties = menuProperties;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {

        if (modelAndView != null && menuIsShown(modelAndView)) {

            final Person signedInUserInModel = (Person) modelAndView.getModelMap().get("signedInUser");
            final Person user = Objects.requireNonNullElseGet(signedInUserInModel, personService::getSignedInUser);
            final String gravatarUrl = user.getGravatarURL();

            modelAndView.addObject("version", applicationVersion);
            modelAndView.addObject("header_referer", request.getHeader("Referer"));

            modelAndView.addObject("userFirstName", user.getFirstName());
            modelAndView.addObject("userLastName", user.getLastName());
            modelAndView.addObject("userId", user.getId());
            modelAndView.addObject("menuGravatarUrl", gravatarUrl);
            modelAndView.addObject("menuHelpUrl", menuProperties.getHelp().getUrl());

            modelAndView.addObject("navigation", createNavigation(user));
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled(user));
            modelAndView.addObject("navigationSickNoteAddAccess", isAllowedToAddOrSubmitSickNote(user));
            modelAndView.addObject("navigationOvertimeItemEnabled", overtimeEnabled(user));
            modelAndView.addObject("gravatarEnabled", settingsService.getSettings().getAvatarSettings().isGravatarEnabled());
        }
    }

    private NavigationDto createNavigation(Person user) {

        final ArrayList<NavigationItemDto> elements = new ArrayList<>();

        elements.add(new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"));
        elements.add(new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"));

        final boolean overtime = overtimeEnabled(user);
        if (overtime) {
            elements.add(new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"));
        }

        final boolean sickNote = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);
        if (sickNote) {
            elements.add(new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"));
        }

        final boolean person = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (person) {
            elements.add(new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"));
        }

        final boolean department = user.hasRole(OFFICE) || user.hasRole(BOSS);
        if (department) {
            elements.add(new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users"));
        }

        final boolean settings = user.hasRole(OFFICE);
        if (settings) {
            elements.add(new NavigationItemDto("settings-link", "/web/settings", "nav.settings.title", "settings", "navigation-settings-link"));
        }

        return new NavigationDto(elements);
    }

    private boolean menuIsShown(ModelAndView modelAndView) {

        final String viewName = modelAndView.getViewName();
        if (viewName == null) {
            return false;
        }

        return !viewName.startsWith("forward:")
            && !viewName.startsWith("redirect:")
            && !viewName.startsWith("login");
    }

    private boolean popupMenuEnabled(Person signedInUser) {
        return signedInUser.hasRole(OFFICE) || overtimeEnabled(signedInUser) || isAllowedToAddOrSubmitSickNote(signedInUser);
    }

    private boolean overtimeEnabled(Person signedInUser) {
        final OvertimeSettings overtimeSettings = settingsService.getSettings().getOvertimeSettings();
        boolean userIsAllowedToWriteOvertime = !overtimeSettings.isOvertimeWritePrivilegedOnly() || signedInUser.isPrivileged();
        return overtimeSettings.isOvertimeActive() && userIsAllowedToWriteOvertime;
    }

    private boolean isAllowedToAddOrSubmitSickNote(Person user) {
        var userIsAllowedToSubmitSickNotes = settingsService.getSettings().getSickNoteSettings().getUserIsAllowedToSubmitSickNotes();
        return user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_ADD) || userIsAllowedToSubmitSickNotes;
    }
}
