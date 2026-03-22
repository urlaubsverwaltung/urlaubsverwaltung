package org.synyx.urlaubsverwaltung.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public FrameDataProvider(
        PersonService personService,
        SettingsService settingsService,
        MenuProperties menuProperties,
        @Value("${info.app.version}") String applicationVersion
    ) {
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
            modelAndView.addObject("navigation", createNavigation(settings, user));
            // TODO not used anymore -> check in favGroup
            modelAndView.addObject("navigationSickNoteAddAccess", isAllowedToAddOrSubmitSickNote(user, settings.getSickNoteSettings()));
            // TODO not used anymore -> check in favGroup
            modelAndView.addObject("navigationOvertimeAddAccess", isUserAllowedToWriteOvertime(user, settings.getOvertimeSettings()));
            modelAndView.addObject("gravatarEnabled", settings.getAvatarSettings().isGravatarEnabled());
        }
    }

    private NavigationDto createNavigation(Settings settings, Person user) {

        final List<NavigationItemDto> favoriteItems = navFavoritesGroup(settings, user);
        final List<NavigationItemDto> basicItems = navBasicGroup(settings);
        final List<NavigationItemDto> companyItems = navCompanyGroup(settings, user);
        final List<NavigationItemDto> settingItems = navSettingsGroup(settings, user);

        return new NavigationDto(favoriteItems, basicItems, companyItems, settingItems);
    }

    private List<NavigationItemDto> navFavoritesGroup(Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final SickNoteSettings sickNoteSettings = settings.getSickNoteSettings();
        final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();

        elements.add(new NavigationItemDto("create-application-link", "/web/application/new", "nav.quick.absence"));

        if (isAllowedToAddOrSubmitSickNote(user, sickNoteSettings)) {
            elements.add(new NavigationItemDto("create-sicknote-link", "/web/sicknote/new", "nav.quick.sicknote"));
        }

        if (isUserAllowedToWriteOvertime(user, overtimeSettings)) {
            elements.add(new NavigationItemDto("create-overtime-link", "/web/overtime/new", "nav.quick.overtime"));
        }

        return elements;
    }

    private List<NavigationItemDto> navBasicGroup(Settings settings) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        // TODO links

        elements.add(new NavigationItemDto("basic-application-link", "/web/application", "nav.basic.absence-todos"));
        elements.add(new NavigationItemDto("basic-absence-overview-link", "/web/absences", "nav.basic.absence-overview"));
        elements.add(new NavigationItemDto("basic-absence-link", "#", "nav.basic.my-absences"));
        elements.add(new NavigationItemDto("basic-sicknote-link", "#", "nav.basic.my-sicknotes"));

        if (overtimeEnabled(settings.getOvertimeSettings())) {
            elements.add(new NavigationItemDto("basic-overtime-link", "#", "nav.basic.my-overtimes"));
        }

        return elements;
    }

    private List<NavigationItemDto> navCompanyGroup(Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        // TODO links

        final boolean canViewPersons = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (canViewPersons) {
            elements.add(new NavigationItemDto("company-person-link", "/web/person", "nav.company.staff", "navigation-persons-link"));
        }

        final boolean canViewDepartments = user.hasRole(OFFICE) || user.hasRole(BOSS);
        if (canViewDepartments) {
            elements.add(new NavigationItemDto("company-department-link", "/web/department", "nav.company.departments"));
        }

        final boolean canViewSickNotes = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);
        if (canViewSickNotes) {
            elements.add(new NavigationItemDto("company-sicknote-link", "/web/sickdays", "nav.company.sicknotes", "navigation-sick-notes-link"));
        }

        // TODO who is allowed to this this?
        if (user.hasRole(OFFICE) && overtimeEnabled(settings.getOvertimeSettings())) {
            elements.add(new NavigationItemDto("company-overtime-link", "/web/overtime", "nav.company.overtimes"));
        }

        return elements;
    }

    private List<NavigationItemDto> navSettingsGroup(Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final boolean canViewSettings = user.hasRole(OFFICE);
        if (canViewSettings) {
            elements.add(new NavigationItemDto("settings-absence-link", "/web/settings/absences", "nav.settings.absence"));
            elements.add(new NavigationItemDto("settings-absencetypes-link", "/web/settings/absence-types", "nav.settings.absenceTypes"));
            elements.add(new NavigationItemDto("settings-overtime-link", "/web/settings/overtime", "nav.settings.overtime"));
            elements.add(new NavigationItemDto("settings-public-holiday-link", "/web/settings/public-holidays", "nav.settings.publicHolidays"));
            elements.add(new NavigationItemDto("settings-holiday-account-link", "/web/settings/account", "nav.settings.account"));
            elements.add(new NavigationItemDto("settings-avatar-link", "/web/settings/avatar", "nav.settings.avatar"));
            elements.add(new NavigationItemDto("settings-calendar-link", "/web/settings/calendar", "nav.settings.calendar"));
            elements.add(new NavigationItemDto("settings-calendar-sync-link", "/web/settings/calendar-sync", "nav.settings.calendarSync"));
        }

        return elements;
    }

    private boolean overtimeEnabled(OvertimeSettings overtimeSettings) {
        return overtimeSettings.isOvertimeActive();
    }

    private boolean isUserAllowedToWriteOvertime(Person signedInUser, OvertimeSettings overtimeSettings) {
        boolean userIsAllowedToWriteOvertime = !overtimeSettings.isOvertimeWritePrivilegedOnly() || signedInUser.isPrivileged();
        return overtimeSettings.isOvertimeActive() && userIsAllowedToWriteOvertime && !overtimeSettings.isOvertimeSyncActive();
    }

    private boolean isAllowedToAddOrSubmitSickNote(Person user, SickNoteSettings sickNoteSettings) {
        var userIsAllowedToSubmitSickNotes = sickNoteSettings.getUserIsAllowedToSubmitSickNotes();
        return user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_ADD) || userIsAllowedToSubmitSickNotes;
    }
}
