package org.synyx.urlaubsverwaltung.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsService;
import org.synyx.urlaubsverwaltung.account.settings.AccountSettingsService;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsService;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsService;
import org.synyx.urlaubsverwaltung.overtime.settings.OvertimeSettingsService;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsService;
import org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.workingtime.settings.WorkingTimeSettingsService;

import javax.servlet.http.HttpServletRequest;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final WorkingTimeSettingsService workingTimeSettingsService;
    private final TimeSettingsService timeSettingsService;
    private final SickNoteSettingsService sickNoteSettingsSerivce;
    private final OvertimeSettingsService overtimeSettingsSerivce;
    private final AccountSettingsService accountSettingsService;
    private final ApplicationSettingsService applicationSettingsService;
    private final CalendarSettingsService calendarSettingsService;
    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    @Autowired
    public SettingsViewController(WorkingTimeSettingsService workingTimeSettingsService,
                                  TimeSettingsService timeSettingsService,
                                  SickNoteSettingsService sickNoteSettingsSerivce,
                                  OvertimeSettingsService overtimeSettingsSerivce,
                                  AccountSettingsService accountSettingsService,
                                  ApplicationSettingsService applicationSettingsService,
                                  CalendarSettingsService calendarSettingsService,
                                  SpecialLeaveSettingsService specialLeaveSettingsService) {

        this.workingTimeSettingsService = workingTimeSettingsService;
        this.timeSettingsService = timeSettingsService;
        this.sickNoteSettingsSerivce = sickNoteSettingsSerivce;
        this.overtimeSettingsSerivce = overtimeSettingsSerivce;
        this.accountSettingsService = accountSettingsService;
        this.applicationSettingsService = applicationSettingsService;
        this.calendarSettingsService = calendarSettingsService;
        this.specialLeaveSettingsService = specialLeaveSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        model.addAttribute("workingtimesettings", workingTimeSettingsService.getSettingsDto());
        model.addAttribute("timesettings", timeSettingsService.getSettingsDto());
        model.addAttribute("sicknotesettings", sickNoteSettingsSerivce.getSettingsDto());
        model.addAttribute("overtimesettings", overtimeSettingsSerivce.getSettingsDto());
        model.addAttribute("accountsettings", accountSettingsService.getSettingsDto());
        model.addAttribute("applicationsettings", applicationSettingsService.getSettingsDto());
        model.addAttribute("calendarsettings", calendarSettingsService.getSettingsDto(request));
        model.addAttribute("specialleavesettings", specialLeaveSettingsService.getSettingsDto());

        return "settings/settings_overview";
    }

//    private boolean shouldShowOAuthError(String googleOAuthError, Settings settings) {
//        return googleOAuthError != null
//            && !googleOAuthError.isEmpty()
//            && settings.getCalendarSettings().getGoogleCalendarSettings().getRefreshToken() == null;
//    }
}
