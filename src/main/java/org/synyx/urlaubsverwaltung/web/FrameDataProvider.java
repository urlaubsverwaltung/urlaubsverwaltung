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
            modelAndView.addObject("navigation", createNavigation(request, settings, user));
            // TODO not used anymore -> check in favGroup
            modelAndView.addObject("navigationSickNoteAddAccess", isAllowedToAddOrSubmitSickNote(user, settings.getSickNoteSettings()));
            // TODO not used anymore -> check in favGroup
            modelAndView.addObject("navigationOvertimeAddAccess", isUserAllowedToWriteOvertime(user, settings.getOvertimeSettings()));
            modelAndView.addObject("gravatarEnabled", settings.getAvatarSettings().isGravatarEnabled());
        }
    }

    private NavigationDto createNavigation(HttpServletRequest request, Settings settings, Person user) {

        final List<NavigationItemDto> favoriteItems = navFavoritesGroup(request, settings, user);
        final List<NavigationItemDto> basicItems = navBasicGroup(request, settings, user);
        final List<NavigationItemDto> companyItems = navCompanyGroup(request, settings, user);
        final List<NavigationItemDto> settingItems = navSettingsGroup(request, settings, user);

        return new NavigationDto(favoriteItems, basicItems, companyItems, settingItems);
    }

    private List<NavigationItemDto> navFavoritesGroup(HttpServletRequest request, Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final String url = request.getRequestURI();

        final SickNoteSettings sickNoteSettings = settings.getSickNoteSettings();
        final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();

        final String application = "/web/application/new";
        elements.add(new NavigationItemDto("create-application-link", application, "nav.quick.absence", url.equals(application)));

        if (isAllowedToAddOrSubmitSickNote(user, sickNoteSettings)) {
            final String sickNote = "/web/sicknote/new";
            elements.add(new NavigationItemDto("create-sicknote-link", sickNote, "nav.quick.sicknote", url.equals(sickNote)));
        }

        if (isUserAllowedToWriteOvertime(user, overtimeSettings)) {
            final String overtime = "/web/overtime/new";
            elements.add(new NavigationItemDto("create-overtime-link", overtime, "nav.quick.overtime", url.equals(overtime)));
        }

        return elements;
    }

    private List<NavigationItemDto> navBasicGroup(HttpServletRequest request, Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final String url = request.getRequestURI();

        final String overview = "/web/person/%d/overview".formatted(user.getId());
        final String application = "/web/application";
        final String absenceOverview = "/web/absences";
        final String myAbsences = "/web/my-absences";
        final String overtime = "/web/overtime";

        elements.add(new NavigationItemDto("basic-overview-link", overview, "nav.basic.overview", url.equals(overview)));
        elements.add(new NavigationItemDto("basic-application-link", application, "nav.basic.absence-todos", url.equals(application)));
        elements.add(new NavigationItemDto("basic-absence-overview-link", absenceOverview, "nav.basic.absence-overview", url.equals(absenceOverview)));
        elements.add(new NavigationItemDto("basic-absence-link", myAbsences, "nav.basic.my-absences", url.equals(myAbsences)));
        elements.add(new NavigationItemDto("basic-sicknote-link", "#", "nav.basic.my-sicknotes", false));

        if (overtimeEnabled(settings.getOvertimeSettings())) {
            elements.add(new NavigationItemDto("basic-overtime-link", overtime, "nav.basic.my-overtimes", url.equals(overtime)));
        }

        return elements;
    }

    private List<NavigationItemDto> navCompanyGroup(HttpServletRequest request, Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final String url = request.getRequestURI();

        final boolean canViewPersons = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (canViewPersons) {
            final String person = "/web/person";
            elements.add(new NavigationItemDto("company-person-link", person, "nav.company.staff", url.equals(person), "navigation-persons-link"));
        }

        final boolean canViewDepartments = user.hasRole(OFFICE) || user.hasRole(BOSS);
        if (canViewDepartments) {
            final String department = "/web/department";
            elements.add(new NavigationItemDto("company-department-link", department, "nav.company.departments", url.equals(department)));
        }

        final boolean canViewApplications = user.hasRole(OFFICE) || user.hasRole(BOSS) || user.hasRole(DEPARTMENT_HEAD) || user.hasRole(SECOND_STAGE_AUTHORITY);
        if (canViewApplications) {
            final String applications = "/web/application/statistics";
            elements.add(new NavigationItemDto("company-application-link", applications, "nav.company.applications", url.equals(applications)));
        }

        final boolean canViewSickNotes = user.hasRole(OFFICE) || user.hasRole(SICK_NOTE_VIEW);
        if (canViewSickNotes) {

            final String sickdays = "/web/sickdays";
            final String statistics = "/web/sicknote/statistics";

            final boolean sickdaysActive = url.equals(sickdays);
            final boolean statisticsActive = url.equals(statistics);
            final boolean rootActive = sickdaysActive || statisticsActive;

            final NavigationItemDto rootItem =
                new NavigationItemDto("company-sicknote-link", sickdays, "nav.company.sicknotes", rootActive, "navigation-sick-notes-link");

            elements.add(rootItem.withSubItems(List.of(
                new NavigationItemDto("company-sicknote-overview-link", sickdays, "nav.company.sicknotes.overview", sickdaysActive),
                new NavigationItemDto("company-sicknote-statistics-link", statistics, "nav.company.sicknotes.statistics", statisticsActive, "navigation-sick-notes-statistics-link")
            )));
        }

        return elements;
    }

    private List<NavigationItemDto> navSettingsGroup(HttpServletRequest request, Settings settings, Person user) {
        final List<NavigationItemDto> elements = new ArrayList<>();

        final boolean canViewSettings = user.hasRole(OFFICE);
        if (canViewSettings) {

            final String url = request.getRequestURI();

            final String absences = "/web/settings/absences";
            final String absencTypes = "/web/settings/absence-types";
            final String overtime = "/web/settings/overtime";
            final String publicHolidays = "/web/settings/public-holidays";
            final String account = "/web/settings/account";
            final String avatar = "/web/settings/avatar";
            final String calendar = "/web/settings/calendar";
            final String calendarSync = "/web/settings/calendar-sync";

            elements.add(new NavigationItemDto("settings-absence-link", absences, "nav.settings.absence", url.equals(absences)));
            elements.add(new NavigationItemDto("settings-absencetypes-link", absencTypes, "nav.settings.absenceTypes", url.equals(absencTypes)));
            elements.add(new NavigationItemDto("settings-overtime-link", overtime, "nav.settings.overtime", url.equals(overtime)));
            elements.add(new NavigationItemDto("settings-public-holiday-link", publicHolidays, "nav.settings.publicHolidays", url.equals(publicHolidays)));
            elements.add(new NavigationItemDto("settings-holiday-account-link", account, "nav.settings.account", url.equals(account)));
            elements.add(new NavigationItemDto("settings-avatar-link", avatar, "nav.settings.avatar", url.equals(avatar)));
            elements.add(new NavigationItemDto("settings-calendar-link", calendar, "nav.settings.calendar", url.equals(calendar)));
            elements.add(new NavigationItemDto("settings-calendar-sync-link", calendarSync, "nav.settings.calendarSync", url.equals(calendarSync)));
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
