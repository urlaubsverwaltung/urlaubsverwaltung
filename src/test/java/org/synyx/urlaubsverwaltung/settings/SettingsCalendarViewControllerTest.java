package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.calendar.TimeSettings;

import java.util.List;
import java.util.TimeZone;

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
class SettingsCalendarViewControllerTest {

    private SettingsCalendarViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsCalendarValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsCalendarViewController(settingsService, settingsValidator);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final TimeSettings timeSettings = new TimeSettings();

        final Settings settings = new Settings();
        settings.setTimeSettings(timeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/calendar"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("timeSettings", sameInstance(timeSettings))
            )))
            .andExpect(model().attribute("availableTimezones", List.of(TimeZone.getAvailableIDs())));
    }

    @Test
    void ensureSaveSettings() throws Exception {

        when(settingsService.getSettings()).thenReturn(new Settings());

        perform(post("/web/settings/calendar")
            .param("id", "42")
            .param("timeSettings.timeZoneId", "Europe/Berlin")
            .param("timeSettings.workDayBeginHour", "8")
            .param("timeSettings.workDayEndHour", "20")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/calendar"))
            .andExpect(flash().attribute("success", true));


        final ArgumentCaptor<Settings> captor = ArgumentCaptor.forClass(Settings.class);
        verify(settingsService).save(captor.capture());

        final Settings actualSettings = captor.getValue();
        assertThat(actualSettings.getTimeSettings()).satisfies(persistedTimeSettings -> {
            assertThat(persistedTimeSettings.getTimeZoneId()).isEqualTo("Europe/Berlin");
            assertThat(persistedTimeSettings.getWorkDayBeginHour()).isEqualTo(8);
            assertThat(persistedTimeSettings.getWorkDayEndHour()).isEqualTo(20);
        });
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
