package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NavigationInterceptorTest {

    @Mock
    private SettingsService settingsService;

    @Test
    void ensureNothingWhenModelAndViewIsNull() {
        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService);
        assertDoesNotThrow(() -> navigationInterceptor.postHandle(null, null, null, null));
    }

    @Test
    void ensureOvertimeIsVisibleWhenActive() throws Exception {

        mockOvertime(true);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationOvertimeItemEnabled", true);
    }

    @Test
    void ensureOvertimeIsNotVisibleWhenDisabled() throws Exception {

        mockOvertime(false);

        final NavigationInterceptor navigationInterceptor = new NavigationInterceptor(settingsService);
        final ModelAndView modelAndView = new ModelAndView();

        navigationInterceptor.postHandle(null, null, null, modelAndView);

        assertThat(modelAndView.getModel()).containsEntry("navigationOvertimeItemEnabled", false);
    }

    private void mockOvertime(boolean overtimeFeatureActive) {
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        overtimeSettings.setOvertimeActive(overtimeFeatureActive);

        final Settings settings = new Settings();
        settings.setOvertimeSettings(overtimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);
    }
}
