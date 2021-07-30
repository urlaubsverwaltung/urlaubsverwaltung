package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsDto;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsService;
import org.synyx.urlaubsverwaltung.account.settings.AccountSettingsDto;
import org.synyx.urlaubsverwaltung.account.settings.AccountSettingsService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsDto;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsService;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsDto;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsService;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsDto;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsDto;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsService;
import org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveSettingsDto;
import org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsDto;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SettingsViewControllerTest {

    private SettingsViewController sut;

    @Mock
    private WorkingTimeSettingsService workingTimeSettingsService;
    @Mock
    private TimeSettingsService timeSettingsService;
    @Mock
    private SickNoteSettingsService sickNoteSettingsService;
    @Mock
    private OvertimeSettingsService overtimeSettingsService;
    @Mock
    private AccountSettingsService accountSettingsService;
    @Mock
    private ApplicationSettingsService applicationSettingsService;
    @Mock
    private CalendarSettingsService calendarSettingsService;
    @Mock
    private SpecialLeaveSettingsService specialLeaveSettingsService;

    @BeforeEach
    void setUp() {
        sut = new SettingsViewController(workingTimeSettingsService, timeSettingsService, sickNoteSettingsService, overtimeSettingsService, accountSettingsService, applicationSettingsService, calendarSettingsService, specialLeaveSettingsService);
    }

    @Test
    void ensureSettingsDetailsFillsModelCorrectly() throws Exception {

        final String requestUrl = "/web/settings";

        final WorkingTimeSettingsDto workingTimeSettingsDto = new WorkingTimeSettingsDto();
        when(workingTimeSettingsService.getSettingsDto()).thenReturn(workingTimeSettingsDto);
        final TimeSettingsDto timeSettingsDto = new TimeSettingsDto();
        when(timeSettingsService.getSettingsDto()).thenReturn(timeSettingsDto);
        final SickNoteSettingsDto sickNoteSettingsDto = new SickNoteSettingsDto();
        when(sickNoteSettingsService.getSettingsDto()).thenReturn(sickNoteSettingsDto);
        final OvertimeSettingsDto overtimeSettingsDto = new OvertimeSettingsDto();
        when(overtimeSettingsService.getSettingsDto()).thenReturn(overtimeSettingsDto);
        final AccountSettingsDto accountSettingsDto = new AccountSettingsDto();
        when(accountSettingsService.getSettingsDto()).thenReturn(accountSettingsDto);
        final ApplicationSettingsDto applicationSettingsDto = new ApplicationSettingsDto();
        when(applicationSettingsService.getSettingsDto()).thenReturn(applicationSettingsDto);
        final CalendarSettingsDto calendarSettingsDto = new CalendarSettingsDto();
        when(calendarSettingsService.getSettingsDto(any(HttpServletRequest.class))).thenReturn(calendarSettingsDto);
        final SpecialLeaveSettingsDto specialLeaveSettingsDto = new SpecialLeaveSettingsDto();
        when(specialLeaveSettingsService.getSettingsDto()).thenReturn(specialLeaveSettingsDto);

        perform(get(requestUrl))
            .andExpect(model().attribute("workingtimesettings", workingTimeSettingsDto))
            .andExpect(model().attribute("timesettings", timeSettingsDto))
            .andExpect(model().attribute("sicknotesettings", sickNoteSettingsDto))
            .andExpect(model().attribute("overtimesettings", overtimeSettingsDto))
            .andExpect(model().attribute("accountsettings", accountSettingsDto))
            .andExpect(model().attribute("applicationsettings", applicationSettingsDto))
            .andExpect(model().attribute("calendarsettings", calendarSettingsDto))
            .andExpect(model().attribute("specialleavesettings", specialLeaveSettingsDto));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }

}
