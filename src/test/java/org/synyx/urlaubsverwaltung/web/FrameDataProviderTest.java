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
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users")
                );
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

        assertThat(modelAndView.getModelMap().get("navigation"))
            .isNotNull()
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users")
                );
            });

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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user"),
                    new NavigationItemDto("department-link", "/web/department", "nav.department.title", "users"),
                    new NavigationItemDto("settings-link", "/web/settings", "nav.settings.title", "settings", "navigation-settings-link")
                );
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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).containsExactly(
                    new NavigationItemDto("home-link", "/web/overview", "nav.home.title", "home"),
                    new NavigationItemDto("application-link", "/web/application", "nav.vacation.title", "calendar"),
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"),
                    new NavigationItemDto("sicknote-link", "/web/sickdays", "nav.sicknote.title", "medkit", "navigation-sick-notes-link"),
                    new NavigationItemDto("person-link", "/web/person", "nav.person.title", "user")
                );
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
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements()).contains(
                    new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock")
                );
            });

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
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements())
                    .isNotEmpty()
                    .doesNotContain(new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"));
            });

        assertThat(modelAndView.getModelMap()).containsEntry("navigationOvertimeAddAccess", false);
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
        mockSettings(overtimeEnabled, overtimeWritePrivilegedOnly, true, false);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry(property, propertyValue);
    }

    @Test
    void ensureQuickAddPopupIsDisabledWhenUserIsLoggedInAndOvertimeIsDisabledAndSubmitSicknotesIsDisabled() {
        mockSettings(false, false, true, false);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", false);
    }

    @Test
    void ensureQuickAddPopupIsEnabledWhenUserIsLoggedInAndOvertimeIsDisabledAndSubmitSicknotesIsEnabled() {
        mockSettings(false, false, true, true);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        final MockHttpServletRequest request = new MockHttpServletRequest();
        sut.postHandle(request, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", true);
        assertThat(modelAndView.getModelMap()).containsEntry("navigationSickNoteAddAccess", true);
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
            .isInstanceOf(NavigationDto.class)
            .satisfies(navigation -> {
                final NavigationDto dto = (NavigationDto) navigation;
                assertThat(dto.elements())
                    .isNotEmpty()
                    .contains(new NavigationItemDto("overtime-link", "/web/overtime", "nav.overtime.title", "clock"));
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
