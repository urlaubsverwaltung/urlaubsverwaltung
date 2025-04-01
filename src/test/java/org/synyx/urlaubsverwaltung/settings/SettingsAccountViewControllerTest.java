package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.DayOfWeek;

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
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;

@ExtendWith(MockitoExtension.class)
class SettingsAccountViewControllerTest {

    private SettingsAccountViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsAccountValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsAccountViewController(settingsService, settingsValidator);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final AccountSettings accountSettings = new AccountSettings();
        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();

        final Settings settings = new Settings();
        settings.setAccountSettings(accountSettings);
        settings.setWorkingTimeSettings(workingTimeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/account"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("accountSettings", sameInstance(accountSettings)),
                hasProperty("workingTimeSettings", sameInstance(workingTimeSettings))
            )))
            .andExpect(model().attribute("weekDays", DayOfWeek.values()));
    }

    @Test
    void ensureSaveSettings() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(post("/web/settings/account")
            .param("id", "42")
            .param("workingTimeSettings.monday", "MORNING")
            .param("workingTimeSettings.tuesday", "MORNING")
            .param("workingTimeSettings.wednesday", "MORNING")
            .param("workingTimeSettings.thursday", "MORNING")
            .param("workingTimeSettings.friday", "MORNING")
            .param("workingTimeSettings.saturday", "MORNING")
            .param("workingTimeSettings.sunday", "MORNING")
            .param("accountSettings.maximumAnnualVacationDays", "6")
            .param("accountSettings.doRemainingVacationDaysExpireGlobally", "true")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/account"))
            .andExpect(flash().attribute("success", true));


        final ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(captor.capture());

        final Settings actualSettings = captor.getValue();
        assertThat(actualSettings.getWorkingTimeSettings()).satisfies(persistedWorkingTimeSettings -> {
            assertThat(persistedWorkingTimeSettings.getMonday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getTuesday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getWednesday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getThursday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getFriday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getSaturday()).isEqualTo(MORNING);
            assertThat(persistedWorkingTimeSettings.getSunday()).isEqualTo(MORNING);
        });
        assertThat(actualSettings.getAccountSettings()).satisfies(persistedAccountSettings -> {
            assertThat(persistedAccountSettings.getMaximumAnnualVacationDays()).isEqualTo(6);
            assertThat(persistedAccountSettings.isDoRemainingVacationDaysExpireGlobally()).isTrue();
        });
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
