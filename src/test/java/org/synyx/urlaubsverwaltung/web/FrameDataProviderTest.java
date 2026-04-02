package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_ADD;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class FrameDataProviderTest {

    private FrameDataProvider sut;

    @Mock
    private PersonService personService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new FrameDataProvider(personService, settingsService, new MenuProperties(), "version");
    }

    @Test
    void postHandleWithoutSignedInUserModel() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("menuGravatarUrl", "https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");
        assertThat(modelAndView.getModelMap()).containsEntry("userId", 10L);
        assertThat(modelAndView.getModelMap()).containsEntry("userFirstName", "Marie");
        assertThat(modelAndView.getModelMap()).containsEntry("userLastName", "Reichenbach");
    }

    @Test
    void postHandleNavigationAccessForBoss() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyDepartmentLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).isEmpty();
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForBossAndSickNoteAdd() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForOffice() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, OFFICE));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createSickNoteLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyDepartmentLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).containsExactlyElementsOf(settingsLinks());
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForDepartmentHead() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).isEmpty();
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForDepartmentHeadAndSickNoteAdd() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createSickNoteLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).isEmpty();
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForSecondStageAuthority() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).isEmpty();
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleNavigationAccessForSecondStageAuthorityAndSickNoteAdd() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setId(10L);
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).containsExactly(
                    createApplicationLink(),
                    createSickNoteLink(),
                    createOvertimeLink()
                );
                assertThat(dto.basic()).containsExactly(
                    basicOverviewLink(10L),
                    basicApplicationLink(),
                    basicAbsenceOverviewLink(),
                    basicAbsenceLink(),
                    basicSickNoteLink(),
                    basicOvertimeLink()
                );
                assertThat(dto.company()).containsExactly(
                    companyPersonLink(),
                    companyApplicationsLink(),
                    companySickNoteLink()
                );
                assertThat(dto.settings()).isEmpty();
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @Test
    void postHandleWithSignedInUserModel() {
        mockSettings(true, false, true, false);

        final Person person = new Person();
        person.setEmail("person@example.org");

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");
        modelAndView.getModelMap().addAttribute("signedInUser", person);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);
        assertThat(modelAndView.getModelMap()).containsEntry("menuGravatarUrl", "https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");

        verifyNoMoreInteractions(personService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"forward::view-name", "redirect::view-name"})
    @NullSource
    void postHandleDoNotAddGravatar(String viewName) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("menuGravatarUrl")).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOvertimeIsSetForOfficeWhenFeatureEnabledAnd(boolean privilegedOnly) {
        mockSettings(true, privilegedOnly, true, false);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOfSatisfying(NavigationDto.class, dto -> assertThat(dto.favorites()).contains(createOvertimeLink()));

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureOvertimeNotSetForOfficeWhenFeatureDisabledAnd(boolean privilegedOnly) {
        mockSettings(false, privilegedOnly, true, false);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .isInstanceOfSatisfying(NavigationDto.class, dto -> assertThat(dto.favorites()).doesNotContain(createOvertimeLink()));

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", false);
    }

    @Test
    void ensureOvertimeItemIsEnabledWhenOvertimeIsRestrictedToPrivileged() {
        mockSettings(true, true, true, false);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOfSatisfying(NavigationDto.class, dto -> {
                assertThat(dto.favorites()).doesNotContain(createOvertimeLink());
                assertThat(dto.basic()).contains(basicOvertimeLink());
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", false);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ensureAvatarSettingsIsInModel(boolean gravatarEnabled) {
        mockSettings(false, false, gravatarEnabled, false);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("gravatarEnabled", gravatarEnabled);
    }

    private static NavigationItemDto createApplicationLink() {
        return createApplicationLink(false);
    }

    private static NavigationItemDto createApplicationLink(boolean active) {
        return new NavigationItemDto("create-application-link", "/web/application/new", "nav.quick.absence", active, "create-application-link");
    }

    private static NavigationItemDto createSickNoteLink() {
        return createSickNoteLink(false);
    }

    private static NavigationItemDto createSickNoteLink(boolean active) {
        return new NavigationItemDto("create-sicknote-link", "/web/sicknote/new", "nav.quick.sicknote", active, "create-sicknote-link");
    }

    private static NavigationItemDto createOvertimeLink() {
        return createOvertimeLink(false);
    }

    private static NavigationItemDto createOvertimeLink(boolean active) {
        return new NavigationItemDto("create-overtime-link", "/web/overtime/new", "nav.quick.overtime", active, "create-overtime-link");
    }

    private static NavigationItemDto basicOverviewLink(long userId) {
        return basicOverviewLink(userId, false);
    }

    private static NavigationItemDto basicOverviewLink(long userId, boolean active) {
        return new NavigationItemDto("basic-overview-link", "/web/person/%d/overview".formatted(userId), "nav.basic.overview", active);
    }

    private static NavigationItemDto basicApplicationLink() {
        return basicApplicationLink(false);
    }

    private static NavigationItemDto basicApplicationLink(boolean active) {
        return new NavigationItemDto("basic-application-link", "/web/application", "nav.basic.absence-todos", active);
    }

    private static NavigationItemDto basicAbsenceOverviewLink() {
        return basicAbsenceOverviewLink(false);
    }

    private static NavigationItemDto basicAbsenceOverviewLink(boolean active) {
        return new NavigationItemDto("basic-absence-overview-link", "/web/absences", "nav.basic.absence-overview", active);
    }

    private static NavigationItemDto basicAbsenceLink() {
        return basicAbsenceLink(false);
    }

    private static NavigationItemDto basicAbsenceLink(boolean active) {
        return new NavigationItemDto("basic-absence-link", "/web/persons/me/applications", "nav.basic.my-absences", active);
    }

    private static NavigationItemDto basicSickNoteLink() {
        return basicSickNoteLink(false);
    }

    private static NavigationItemDto basicSickNoteLink(boolean active) {
        return new NavigationItemDto("basic-sicknote-link", "/web/persons/me/sicknotes", "nav.basic.my-sicknotes", active);
    }

    private static NavigationItemDto basicOvertimeLink() {
        return basicOvertimeLink(false);
    }

    private static NavigationItemDto basicOvertimeLink(boolean active) {
        return new NavigationItemDto("basic-overtime-link", "/web/overtime", "nav.basic.my-overtimes", active);
    }

    private static NavigationItemDto companyPersonLink() {
        return companyPersonLink(false);
    }

    private static NavigationItemDto companyPersonLink(boolean active) {
        return new NavigationItemDto("company-person-link", "/web/person", "nav.company.staff", active, "navigation-persons-link");
    }

    private static NavigationItemDto companyApplicationsLink() {
        return companyApplicationsLink(false);
    }

    private static NavigationItemDto companyApplicationsLink(boolean active) {
        return new NavigationItemDto("company-application-link", "/web/application/statistics", "nav.company.applications", active);
    }

    private static NavigationItemDto companyDepartmentLink() {
        return companyDepartmentLink(false);
    }

    private static NavigationItemDto companyDepartmentLink(boolean active) {
        return new NavigationItemDto("company-department-link", "/web/department", "nav.company.departments", active);
    }

    private static NavigationItemDto companySickNoteLink() {
        return companySickNoteLink(false);
    }

    private static NavigationItemDto companySickNoteLink(boolean active) {
        return new NavigationItemDto("company-sicknote-link", "/web/sickdays", "nav.company.sicknotes", active, "navigation-sick-notes-link")
            .withSubItems(
                List.of(
                    new NavigationItemDto("company-sicknote-overview-link", "/web/sickdays", "nav.company.sicknotes.overview", active),
                    new NavigationItemDto("company-sicknote-statistics-link", "/web/sicknote/statistics", "nav.company.sicknotes.statistics", false, "navigation-sick-notes-statistics-link")
                )
            );
    }

    private static List<NavigationItemDto> settingsLinks() {
        return List.of(
            new NavigationItemDto("settings-absence-link", "/web/settings/absences", "nav.settings.absence", false, "settings-absence-link"),
            new NavigationItemDto("settings-absencetypes-link", "/web/settings/absence-types", "nav.settings.absenceTypes", false, "settings-absencetypes-link"),
            new NavigationItemDto("settings-overtime-link", "/web/settings/overtime", "nav.settings.overtime", false, "settings-overtime-link"),
            new NavigationItemDto("settings-public-holiday-link", "/web/settings/public-holidays", "nav.settings.publicHolidays", false, "settings-public-holiday-link"),
            new NavigationItemDto("settings-holiday-account-link", "/web/settings/account", "nav.settings.account", false, "settings-holiday-account-link"),
            new NavigationItemDto("settings-avatar-link", "/web/settings/avatar", "nav.settings.avatar", false, "settings-avatar-link"),
            new NavigationItemDto("settings-calendar-link", "/web/settings/calendar", "nav.settings.calendar", false, "settings-calendar-link"),
            new NavigationItemDto("settings-calendar-sync-link", "/web/settings/calendar-sync", "nav.settings.calendarSync", false, "settings-calendar-sync-link")
        );
    }

    private void mockSettings(boolean overtimeFeatureActive, boolean overtimeWritePrivilegedOnly, boolean gravatarEnabled, boolean submitSicknotesEnabled) {
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeFeatureActive);
        overtimeSettings.setOvertimeWritePrivilegedOnly(overtimeWritePrivilegedOnly);

        final AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(gravatarEnabled);

        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(submitSicknotesEnabled);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);
        settings.setAvatarSettings(avatarSettings);
        settings.setSickNoteSettings(sickNoteSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }
}
