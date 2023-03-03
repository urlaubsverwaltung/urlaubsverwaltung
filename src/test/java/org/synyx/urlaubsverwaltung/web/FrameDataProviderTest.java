package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.stream.Stream;

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
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap()).containsEntry("menuGravatarUrl", "https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");
        assertThat(modelAndView.getModelMap()).containsEntry("userId", 10);
        assertThat(modelAndView.getModelMap()).containsEntry("userFirstName", "Marie");
        assertThat(modelAndView.getModelMap()).containsEntry("userLastName", "Reichenbach");
    }

    @Test
    void postHandleNavigationAccessForBoss() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForBossAndSickNoteAdd() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, BOSS, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForOffice() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, OFFICE));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users"),
                    new NavigationItemDto("settings-link", "/web/settings", "nav.settings.title", "settings", "navigation-settings-link")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForDepartmentHead() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForDepartmentHeadAndSickNoteAdd() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, DEPARTMENT_HEAD, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForSecondStageAuthority() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", false);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleNavigationAccessForSecondStageAuthorityAndSickNoteAdd() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setId(10);
        person.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY, SICK_NOTE_VIEW, SICK_NOTE_ADD));
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-new-link", "/web/application/new", "nav.apply.title", "plus-circle"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void postHandleWithSignedInUserModel() {
        mockOvertime(true, false);

        final Person person = new Person();
        person.setEmail("person@example.org");

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");
        modelAndView.getModelMap().addAttribute("signedInUser", person);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap()).containsEntry("menuGravatarUrl", "https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");

        verifyNoMoreInteractions(personService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"redirect:", "login"})
    @NullSource
    void postHandleDoNotAddGravatar(String viewName) {

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("menuGravatarUrl")).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void ensureOvertimeIsSetForOfficeWhenFeatureEnabledAnd(boolean privilegedOnly) {
        mockOvertime(true, privilegedOnly);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).contains(
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void ensureOvertimeNotSetForOfficeWhenFeatureDisabledAnd(boolean privilegedOnly) {
        mockOvertime(false, privilegedOnly);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).doesNotContain(
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", false);
    }

    static Stream<Arguments> modelPropertiesNavigation() {
        return Stream.of(
            Arguments.of("navigationRequestPopupEnabled", true, false, true),
            Arguments.of("navigationRequestPopupEnabled", true, false, false),
            Arguments.of("navigationRequestPopupEnabled", true, true, true),
            Arguments.of("navigationRequestPopupEnabled", true, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("modelPropertiesNavigation")
    void ensureForOvertimeAndRequestIsCorrectSetForOffice(String property, boolean propertyValue, boolean overtimeEnabled, boolean overtimeWritePrivilegedOnly) {
        mockOvertime(overtimeEnabled, overtimeWritePrivilegedOnly);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry(property, propertyValue);
    }

    @Test
    void ensureQuickAddPopupIsDisabledWhenUserIsLoggedInAndOvertimeIsDisabled() {
        mockOvertime(false, false);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", false);
    }

    @Test
    void ensureOvertimeItemIsNotEnabledWhenOvertimeIsRestrictedToPrivileged() {
        mockOvertime(true, true);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.getElements()).doesNotContain(
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "briefcase")
                );
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeItemEnabled", false);
    }

    private void mockOvertime(boolean overtimeFeatureActive, boolean overtimeWritePrivilegedOnly) {
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeFeatureActive);
        overtimeSettings.setOvertimeWritePrivilegedOnly(overtimeWritePrivilegedOnly);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }
}
