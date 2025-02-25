package org.synyx.urlaubsverwaltung.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

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
public class FrameDataProvider implements DataProviderInterface {

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

        if (addDataIf(modelAndView)) {

            final Person signedInUserInModel = (Person) modelAndView.getModelMap().get("signedInUser");
            final Person user = Objects.requireNonNullElseGet(signedInUserInModel, personService::getSignedInUser);
            final String gravatarUrl = user.getGravatarURL();

            modelAndView.addObject("version", applicationVersion);
            modelAndView.addObject("header_referer", request.getHeader("Referer"));

            modelAndView.addObject("userFirstName", user.getFirstName());
            modelAndView.addObject("userInitials", user.getInitials());
            modelAndView.addObject("userLastName", user.getLastName());
            modelAndView.addObject("userId", user.getId());
            modelAndView.addObject("menuGravatarUrl", gravatarUrl);
            modelAndView.addObject("menuHelpUrl", menuProperties.getHelp().getUrl());

            final Settings settings = settingsService.getSettings();
            modelAndView.addObject("navigation", createNavigation(user, settings));
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled(user, settings));
            modelAndView.addObject("navigationSickNoteAddAccess", isAllowedToAddOrSubmitSickNote(user, settings.getSickNoteSettings()));
            modelAndView.addObject("navigationOvertimeAddAccess", isUserAllowedToWriteOvertime(user, settings.getOvertimeSettings()));
            modelAndView.addObject("gravatarEnabled", settings.getAvatarSettings().isGravatarEnabled());
        }
    }

    private NavigationDto createNavigation(Person user, Settings settings) {

        final ArrayList<NavigationItemDto> elements = new ArrayList<>();

        elements.add(new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"));
        elements.add(new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"));

        final boolean overtimeIsEnabled = overtimeEnabled(settings.getOvertimeSettings());
        if (overtimeIsEnabled) {
            elements.add(new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"));
        }

        final boolean canViewSickNotes = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);
        if (canViewSickNotes) {
            elements.add(new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"));
        }

        final boolean canViewPersons = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (canViewPersons) {
            elements.add(new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"));
        }

        final boolean canViewDepartments = user.hasRole(OFFICE) || user.hasRole(BOSS);
        if (canViewDepartments) {
            elements.add(new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users"));
        }

        final boolean canViewSettings = user.hasRole(OFFICE);
        if (canViewSettings) {
            elements.add(new NavigationItemDto("settings-link", "/web/settings", "nav.settings.title", "settings", "navigation-settings-link"));
        }

        return new NavigationDto(elements);
    }

    private boolean popupMenuEnabled(Person signedInUser, Settings settings) {
        return signedInUser.hasRole(OFFICE) || isUserAllowedToWriteOvertime(signedInUser, settings.getOvertimeSettings()) || isAllowedToAddOrSubmitSickNote(signedInUser, settings.getSickNoteSettings());
    }

    private boolean overtimeEnabled(OvertimeSettings overtimeSettings) {
        return overtimeSettings.isOvertimeActive();
    }

    private boolean isUserAllowedToWriteOvertime(Person signedInUser, OvertimeSettings overtimeSettings) {
        boolean userIsAllowedToWriteOvertime = !overtimeSettings.isOvertimeWritePrivilegedOnly() || signedInUser.isPrivileged();
        return overtimeSettings.isOvertimeActive() && userIsAllowedToWriteOvertime;
    }

    private boolean isAllowedToAddOrSubmitSickNote(Person user, SickNoteSettings sickNoteSettings) {
        var userIsAllowedToSubmitSickNotes = sickNoteSettings.getUserIsAllowedToSubmitSickNotes();
        return user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_ADD) || userIsAllowedToSubmitSickNotes;
    }
}
