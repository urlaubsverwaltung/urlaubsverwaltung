package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.time.DayOfWeek;
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
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN;

@ExtendWith(MockitoExtension.class)
class SettingsWorkingTimeViewControllerTest {

    private SettingsWorkingTimeViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private SettingsWorkingTimeValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsWorkingTimeViewController(settingsService, settingsValidator);
    }

    @Test
    void ensureGetSettings() throws Exception {

        final WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        final OvertimeSettings overtimeSettings = new OvertimeSettings();
        final TimeSettings timeSettings = new TimeSettings();

        final Settings settings = new Settings();
        settings.setWorkingTimeSettings(workingTimeSettings);
        settings.setOvertimeSettings(overtimeSettings);
        settings.setTimeSettings(timeSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(get("/web/settings/working-time"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("settings", allOf(
                hasProperty("workingTimeSettings", sameInstance(workingTimeSettings)),
                hasProperty("overtimeSettings", sameInstance(overtimeSettings)),
                hasProperty("timeSettings", sameInstance(timeSettings))
            )))
            .andExpect(model().attribute("availableTimezones", List.of(TimeZone.getAvailableIDs())))
            .andExpect(model().attribute("federalStateTypes", FederalState.federalStatesTypesByCountry()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()))
            .andExpect(model().attribute("weekDays", DayOfWeek.values()));
    }

    @Test
    void ensureSaveSettings() throws Exception {

        final ApplicationSettings applicationSettings = new ApplicationSettings();
        final AccountSettings accountSettings = new AccountSettings();
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        final AvatarSettings avatarSettings = new AvatarSettings();

        final Settings settings = new Settings();
        settings.setApplicationSettings(applicationSettings);
        settings.setAccountSettings(accountSettings);
        settings.setSickNoteSettings(sickNoteSettings);
        settings.setAvatarSettings(avatarSettings);

        when(settingsService.getSettings()).thenReturn(settings);

        perform(post("/web/settings/working-time")
            .param("id", "42")
            .param("workingTimeSettings.monday", "MORNING")
            .param("workingTimeSettings.tuesday", "MORNING")
            .param("workingTimeSettings.wednesday", "MORNING")
            .param("workingTimeSettings.thursday", "MORNING")
            .param("workingTimeSettings.friday", "MORNING")
            .param("workingTimeSettings.saturday", "MORNING")
            .param("workingTimeSettings.sunday", "MORNING")
            .param("workingTimeSettings.workingDurationForChristmasEve", "ZERO")
            .param("workingTimeSettings.workingDurationForNewYearsEve", "ZERO")
            .param("workingTimeSettings.federalState", "GERMANY_BAYERN")
            .param("timeSettings.timeZoneId", "Europe/Berlin")
            .param("timeSettings.workDayBeginHour", "6")
            .param("timeSettings.workDayEndHour", "18")
            .param("overtimeSettings.overtimeActive", "true")
            .param("overtimeSettings.overtimeReductionWithoutApplicationActive", "true")
            .param("overtimeSettings.maximumOvertime", "1")
            .param("overtimeSettings.minimumOvertime", "2")
            .param("overtimeSettings.minimumOvertimeReduction", "3")
            .param("overtimeSettings.overtimeWritePrivilegedOnly", "true")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/settings/working-time"))
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
            assertThat(persistedWorkingTimeSettings.getWorkingDurationForChristmasEve()).isEqualTo(ZERO);
            assertThat(persistedWorkingTimeSettings.getWorkingDurationForNewYearsEve()).isEqualTo(ZERO);
            assertThat(persistedWorkingTimeSettings.getFederalState()).isEqualTo(GERMANY_BAYERN);
        });
        assertThat(actualSettings.getTimeSettings()).satisfies(persistedTimeSettings -> {
            assertThat(persistedTimeSettings.getTimeZoneId()).isEqualTo("Europe/Berlin");
            assertThat(persistedTimeSettings.getWorkDayBeginHour()).isEqualTo(6);
            assertThat(persistedTimeSettings.getWorkDayEndHour()).isEqualTo(18);
        });
        assertThat(actualSettings.getOvertimeSettings()).satisfies(persistedOvertimeSettings -> {
            assertThat(persistedOvertimeSettings.isOvertimeActive()).isTrue();
            assertThat(persistedOvertimeSettings.isOvertimeReductionWithoutApplicationActive()).isTrue();
            assertThat(persistedOvertimeSettings.getMaximumOvertime()).isEqualTo(1);
            assertThat(persistedOvertimeSettings.getMinimumOvertime()).isEqualTo(2);
            assertThat(persistedOvertimeSettings.getMinimumOvertimeReduction()).isEqualTo(3);
            assertThat(persistedOvertimeSettings.isOvertimeWritePrivilegedOnly()).isTrue();
        });
        assertThat(actualSettings.getApplicationSettings()).isSameAs(applicationSettings);
        assertThat(actualSettings.getAccountSettings()).isSameAs(accountSettings);
        assertThat(actualSettings.getSickNoteSettings()).isSameAs(sickNoteSettings);
        assertThat(actualSettings.getAvatarSettings()).isSameAs(avatarSettings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
