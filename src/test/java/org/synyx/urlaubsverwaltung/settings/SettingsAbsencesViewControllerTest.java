package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SettingsAbsencesViewControllerTest {

    private SettingsAbsencesViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsAbsencesValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsAbsencesViewController(settingsService, settingsValidator);
    }

    @Test
    void ensureViewModel() throws Exception {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();

        final Settings settings = new Settings();
        settings.setApplicationSettings(applicationSettings);
        settings.setSickNoteSettings(sickNoteSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/absences"))
            .andExpect(model().attribute("settings", allOf(
                hasProperty("applicationSettings", sameInstance(applicationSettings)),
                hasProperty("sickNoteSettings", sameInstance(sickNoteSettings))
            )));
    }

    @Test
    void ensureSettingsSaved() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(
            post("/web/settings/absences")
                .param("id", "1337")
                .param("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", "1")
                .param("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", "2")
                .param("applicationSettings.daysBeforeRemindForWaitingApplications", "3")
                .param("applicationSettings.daysBeforeRemindForUpcomingApplications", "4")
                .param("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", "5")
                .param("applicationSettings.remindForWaitingApplications", "true")
                .param("applicationSettings.allowHalfDays", "true")
                .param("applicationSettings.remindForUpcomingApplications", "true")
                .param("applicationSettings.remindForUpcomingHolidayReplacement", "true")
                .param("sickNoteSettings.maximumSickPayDays", "7")
                .param("sickNoteSettings.daysBeforeEndOfSickPayNotification", "8")
        )
            .andExpect(status().isFound())
            .andExpect(flash().attribute("success", true))
            .andExpect(redirectedUrl("/web/settings/absences"));

        final ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(settingsCaptor.capture());

        final Settings actualSettings = settingsCaptor.getValue();
        assertThat(actualSettings.getId()).isEqualTo(1337L);
        assertThat(actualSettings.getApplicationSettings()).satisfies(applicationSettings -> {
            assertThat(applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance()).isEqualTo(1);
            assertThat(applicationSettings.getMaximumMonthsToApplyForLeaveAfterwards()).isEqualTo(2);
            assertThat(applicationSettings.getDaysBeforeRemindForWaitingApplications()).isEqualTo(3);
            assertThat(applicationSettings.getDaysBeforeRemindForUpcomingApplications()).isEqualTo(4);
            assertThat(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement()).isEqualTo(5);
            assertThat(applicationSettings.isRemindForWaitingApplications()).isTrue();
            assertThat(applicationSettings.isAllowHalfDays()).isTrue();
            assertThat(applicationSettings.isRemindForUpcomingApplications()).isTrue();
            assertThat(applicationSettings.isRemindForUpcomingHolidayReplacement()).isTrue();
        });
        assertThat(actualSettings.getSickNoteSettings()).satisfies(sickNoteSettings -> {
            assertThat(sickNoteSettings.getMaximumSickPayDays()).isEqualTo(7);
            assertThat(sickNoteSettings.getDaysBeforeEndOfSickPayNotification()).isEqualTo(8);
        });
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
