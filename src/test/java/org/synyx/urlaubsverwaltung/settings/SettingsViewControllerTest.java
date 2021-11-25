package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SettingsViewControllerTest {

    private SettingsViewController sut;

    @Mock
    private SettingsService settingsService;
    @Mock
    private VacationTypeService vacationTypeService;
    @Mock
    private SettingsValidator settingsValidator;

    @BeforeEach
    void setUp() {
        sut = new SettingsViewController(settingsService, vacationTypeService, settingsValidator, "version");
    }

    @Test
    void ensureSettingsDetailsFillsModelCorrectly() throws Exception {

        final VacationType vacationType = new VacationType();
        vacationType.setId(1);
        vacationType.setActive(true);
        vacationType.setRequiresApproval(true);
        vacationType.setCategory(VacationCategory.HOLIDAY);
        vacationType.setMessageKey("vacationType.messageKey");
        when(vacationTypeService.getAllVacationTypes()).thenReturn(List.of(vacationType));

        final Settings settings = someSettings();
        when(settingsService.getSettings()).thenReturn(settings);

        final String requestUrl = "/web/settings";

        // TODO test explicit settings attributes
        perform(get(requestUrl))
            .andExpect(model().attributeExists("settings"))
            .andExpect(model().attribute("federalStateTypes", FederalState.values()))
            .andExpect(model().attribute("dayLengthTypes", DayLength.values()));
    }

    @Test
    void ensureSettingsDetailsUsesCorrectView() throws Exception {

        when(settingsService.getSettings()).thenReturn(someSettings());

        perform(get("/web/settings")).andExpect(view().name("settings/settings_form"));
    }

    @Test
    void ensureSettingsSavedShowsFormIfValidationFails() throws Exception {

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.rejectValue("applicationSettings", "error");
            return null;
        }).when(settingsValidator).validate(any(), any());

        perform(post("/web/settings"))
            .andExpect(view().name("settings/settings_form"));
    }

    @Test
    void ensureSettingsSavedSavesSettingsIfValidationSuccessfully() throws Exception {

        perform(post("/web/settings")
            .param("absenceTypeSettings.items[0].id", "10"));

        verify(settingsService).save(any(Settings.class));
    }

    @Test
    void ensureSettingsSavedAddFlashAttributeAndRedirectsToSettings() throws Exception {

        perform(post("/web/settings")
            .param("absenceTypeSettings.items[0].id", "10"))
            .andExpect(flash().attribute("success", true));

        perform(post("/web/settings")
            .param("absenceTypeSettings.items[0].id", "10"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/web/settings"));
    }

    private static Settings someSettings() {

        final Settings settings = new Settings();

        settings.setId(1);
        settings.setApplicationSettings(new ApplicationSettings());
        settings.setAccountSettings(new AccountSettings());
        settings.setWorkingTimeSettings(new WorkingTimeSettings());
        settings.setOvertimeSettings(new OvertimeSettings());
        settings.setTimeSettings(new TimeSettings());
        settings.setSickNoteSettings(new SickNoteSettings());

        return settings;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
