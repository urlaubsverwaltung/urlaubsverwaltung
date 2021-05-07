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
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class MenuDataProviderTest {

    private MenuDataProvider sut;

    @Mock
    private PersonService personService;

    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        sut = new MenuDataProvider(personService, settingsService);
    }

    @Test
    void postHandleWithoutSignedInUserModel() {
        mockOvertime(true);

        final Person person = new Person();
        person.setId(10);
        person.setFirstName("Marie");
        person.setLastName("Reichenbach");
        person.setEmail("person@example.org");
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("menuGravatarUrl")).isEqualTo("https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");
        assertThat(modelAndView.getModelMap().get("userId")).isEqualTo(10);
        assertThat(modelAndView.getModelMap().get("userNiceName")).isEqualTo("Marie Reichenbach");
    }

    @Test
    void postHandleWithSignedInUserModel() {
        mockOvertime(true);

        final Person person = new Person();
        person.setEmail("person@example.org");

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");
        modelAndView.getModelMap().addAttribute("signedInUser", person);

        sut.postHandle(null, null, null, modelAndView);
        assertThat(modelAndView.getModelMap().get("menuGravatarUrl")).isEqualTo("https://gravatar.com/avatar/f651d5c5f6f68c5b13f2846da4ea544b");

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

    static Stream<Arguments> modelPropertiesNavigation() {
        return Stream.of(
            Arguments.of("navigationOvertimeItemEnabled", true, true),
            Arguments.of("navigationOvertimeItemEnabled", false, false),
            Arguments.of("navigationRequestPopupEnabled", true, false),
            Arguments.of("navigationRequestPopupEnabled", true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("modelPropertiesNavigation")
    void ensureForOvertimeAndRequestIsCorrectSetForOffice(String property, boolean propertyValue, boolean overtimeEnabled) {
        mockOvertime(overtimeEnabled);

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
        mockOvertime(false);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("someView");

        sut.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", false);
    }

    private void mockOvertime(boolean overtimeFeatureActive) {
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeFeatureActive);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }
}
