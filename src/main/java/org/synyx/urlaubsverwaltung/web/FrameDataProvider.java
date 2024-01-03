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
import java.util.List;
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
            modelAndView.addObject("userLastName", user.getLastName());
            modelAndView.addObject("userId", user.getId());
            modelAndView.addObject("menuGravatarUrl", gravatarUrl);
            modelAndView.addObject("menuHelpUrl", menuProperties.getHelp().getUrl());

            final Settings settings = settingsService.getSettings();
            modelAndView.addObject("addThingNavigation", addThingNavigation(user, settings));
            modelAndView.addObject("navigation", navigation(user, settings, request));
            modelAndView.addObject("navigationRequestPopupEnabled", popupMenuEnabled(user, settings));
            modelAndView.addObject("navigationSickNoteAddAccess", isAllowedToAddOrSubmitSickNote(user, settings.getSickNoteSettings()));
            modelAndView.addObject("navigationOvertimeAddAccess", isUserAllowedToWriteOvertime(user, settings.getOvertimeSettings()));
            modelAndView.addObject("gravatarEnabled", settings.getAvatarSettings().isGravatarEnabled());
        }
    }

    private NavigationDto addThingNavigation(Person user, Settings settings) {

        final boolean overtime = overtimeEnabled(settings.getOvertimeSettings());
        final boolean sickNote = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);

        final ArrayList<NavigationItemDto> elements = new ArrayList<>();

        elements.add(new NavigationItemDto("add-application", "/web/application/new", "nav.add.vacation", "plus", false));

        if (sickNote) {
            elements.add(new NavigationItemDto("add-sick-note", "/web/sicknote/new", "nav.add.sicknote", "plus", false));
        }

        if (overtime) {
            elements.add(new NavigationItemDto("add-overtime", "/web/overtime/new", "nav.add.overtime", "plus", false));
        }

        return new NavigationDto(elements);
    }

    private NavigationDto navigation(Person user, Settings settings, HttpServletRequest request) {

        final String url = request.getRequestURI();
        final ArrayList<NavigationItemDto> elements = new ArrayList<>();

        final String homePath = "/web/overview";
        final boolean homeActive = url.matches("/web/person/.+/overview");
        elements.add(new NavigationItemDto("home-link", homePath, "nav.home.title", "home", homeActive));

        final String applicationPath = "/web/application";
        final boolean applicationActive = url.startsWith(applicationPath);
        elements.add(new NavigationItemDto("application-link", applicationPath, "nav.vacation.title", "calendar", applicationActive));

        final boolean overtimeIsEnabled = overtimeEnabled(settings.getOvertimeSettings());
        if (overtimeIsEnabled) {
            final String overtimePath = "/web/overtime";
            final boolean overtimeActive = url.startsWith(overtimePath);
            elements.add(new NavigationItemDto("overtime-link", overtimePath, "nav.overtime.title", "clock", overtimeActive));
        }

        final boolean canViewSickNotes = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);
        if (canViewSickNotes) {
            final String sickNotesPath = "/web/sickdays";
            final boolean sickNotesActive = url.startsWith(sickNotesPath);
            elements.add(new NavigationItemDto("sicknote-link", sickNotesPath, "nav.sicknote.title", "medkit", sickNotesActive, "navigation-sick-notes-link"));
        }

        final boolean canViewPersons = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (canViewPersons) {
            final String personsPath = "/web/person";
            final boolean personsActive = !homeActive && url.startsWith(personsPath);
            elements.add(new NavigationItemDto("person-link", personsPath, "nav.person.title", "user", personsActive));
        }

        final boolean canViewDepartments = user.hasRole(OFFICE) || user.hasRole(BOSS);
        if (canViewDepartments) {
            final String departmentsPath = "/web/department";
            final boolean departmentsActive = url.startsWith(departmentsPath);
            elements.add(new NavigationItemDto("department-link", departmentsPath, "nav.department.title", "users", departmentsActive));
        }

        final boolean canViewSettings = user.hasRole(OFFICE);
        if (canViewSettings) {
            final String settingsPath = "/web/settings";
            final boolean settingsActive = url.startsWith(settingsPath);
            final NavigationDto subnav = settingsSubNavigation(url);
            elements.add(new NavigationItemDto("settings-link", settingsPath, "nav.settings.title", "settings", settingsActive, subnav, "navigation-settings-link"));
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

    private NavigationDto settingsSubNavigation(String url) {

        final String absencesLink = "/web/settings/absences";
        final String absenceTypesLink = "/web/settings/absence-types";
        final String workingTimeLink = "/web/settings/working-time";
        final String avatarLink = "/web/settings/avatar";
        final String calendarSyncLink = "/web/settings/calendar-sync";

        final List<NavigationItemDto> elements =  List.of(
            new NavigationItemDto("settings-absences-link", absencesLink, "nav.settings.title", "circle", url.equals(absencesLink)),
            new NavigationItemDto("settings-absence-types-link", absenceTypesLink, "nav.settings.title", "circle", url.equals(absenceTypesLink)),
            new NavigationItemDto("settings-working-time-link", workingTimeLink, "nav.settings.title", "circle", url.equals(workingTimeLink)),
            new NavigationItemDto("settings-avatar-link", avatarLink, "nav.settings.title", "circle", url.equals(avatarLink)),
            new NavigationItemDto("settings-calendar-sync-link", calendarSyncLink, "nav.settings.title", "circle", url.equals(calendarSyncLink))
        );

        return new NavigationDto(elements);
    }
}
