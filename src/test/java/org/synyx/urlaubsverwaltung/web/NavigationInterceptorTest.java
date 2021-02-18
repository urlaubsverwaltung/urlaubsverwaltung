package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class NavigationInterceptorTest {

    @Mock
    private SettingsService settingsService;

    @Mock
    private PersonService personService;

    @Test
    void ensureNothingWhenModelAndViewIsNull() {
        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);

        assertDoesNotThrow(() -> navigationInterceptor.postHandle(null, null, null, null));
    }

    @Test
    void ensureOvertimeIsVisibleWhenActive() throws Exception {

        mockOvertime(true);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void ensureOvertimeIsNotVisibleWhenDisabled() throws Exception {

        mockOvertime(false);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationOvertimeItemEnabled", false);
    }

    @Test
    void ensureQuickAddPopupIsEnabledWhenOfficeIsLoggedInAndOvertimeIsDisabled() throws Exception {
        mockOvertime(false);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", true);
    }

    @Test
    void ensureQuickAddPopupIsEnabledWhenUserIsLoggedInAndOvertimeIsEnabled() throws Exception {
        mockOvertime(true);

        final Person person = new Person();
        person.setPermissions(List.of(OFFICE));
        when(personService.getSignedInUser()).thenReturn(person);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", true);
    }

    @Test
    void ensureQuickAddPopupIsDisabledWhenUserIsLoggedInAndOvertimeIsDisabled() throws Exception {
        mockOvertime(false);

        final Person person = new Person();
        person.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(person);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationRequestPopupEnabled", false);
    }

    @Test
    void ensureDoesNotThrowWhenThereIsNoSignedInUser() {
        mockOvertime(false);

        when(personService.getSignedInUser()).thenThrow();

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService, personService);
        final ModelAndView modelAndView = new ModelAndView();

        assertDoesNotThrow(() -> navigationInterceptor.postHandle(null, null, null, modelAndView));
    }

    private void mockOvertime(boolean overtimeFeatureActive) {
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeFeatureActive);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }
}
