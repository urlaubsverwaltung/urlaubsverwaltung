package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.AbsenceSettings;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.settings.WorkingTimeSettings;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;


/**
 * Controller to manage the application's {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 *
 * @author  Daniel Hammann - hammann@synyx.de
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private CalendarSyncService calendarSyncService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AbsenceSettingsValidator absenceSettingsValidator;

    @Autowired
    private CalendarSettingsValidator calendarSettingsValidator;

    @Autowired
    private MailSettingsValidator mailSettingsValidator;

    @Autowired
    private WorkingTimeSettingsValidator workingTimeSettingsValidator;

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public String settingsDetails(Model model) {

        model.addAttribute("settings", settingsService.getSettings());

        return "settings/settings_details";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/absence", method = RequestMethod.GET)
    public String absenceSettings(Model model) {

        model.addAttribute("settings", settingsService.getSettings().getAbsenceSettings());

        return "settings/absence_settings_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/absence", method = RequestMethod.POST)
    public String absenceSettings(@ModelAttribute("settings") AbsenceSettings absenceSettings, Errors errors,
        Model model, RedirectAttributes redirectAttributes) {

        absenceSettingsValidator.validate(absenceSettings, errors);

        if (errors.hasErrors()) {
            return "settings/absence_settings_form";
        }

        settingsService.save(absenceSettings);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/settings";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/workingtime", method = RequestMethod.GET)
    public String workingTimeSettings(Model model) {

        model.addAttribute("settings", settingsService.getSettings().getWorkingTimeSettings());
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());

        return "settings/workingtime_settings_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/workingtime", method = RequestMethod.POST)
    public String workingTimeSettings(@ModelAttribute("settings") WorkingTimeSettings workingTimeSettings,
        Errors errors, Model model, RedirectAttributes redirectAttributes) {

        workingTimeSettingsValidator.validate(workingTimeSettings, errors);

        if (errors.hasErrors()) {
            model.addAttribute("federalStateTypes", FederalState.values());
            model.addAttribute("dayLengthTypes", DayLength.values());

            return "settings/workingtime_settings_form";
        }

        settingsService.save(workingTimeSettings);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/settings";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/mail", method = RequestMethod.GET)
    public String mailSettings(Model model) {

        model.addAttribute("settings", settingsService.getSettings().getMailSettings());

        return "settings/mail_settings_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/mail", method = RequestMethod.POST)
    public String mailSettings(@ModelAttribute("settings") MailSettings mailSettings, Errors errors, Model model,
        RedirectAttributes redirectAttributes) {

        mailSettingsValidator.validate(mailSettings, errors);

        if (errors.hasErrors()) {
            return "settings/mail_settings_form";
        }

        settingsService.save(mailSettings);

        mailService.sendSuccessfullyUpdatedSettingsNotification(mailSettings);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/settings";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/calendar", method = RequestMethod.GET)
    public String calendarSettings(Model model) {

        model.addAttribute("settings", settingsService.getSettings().getCalendarSettings());

        return "settings/calendar_settings_form";
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/settings/calendar", method = RequestMethod.POST)
    public String calendarSettings(@ModelAttribute("settings") CalendarSettings calendarSettings, Errors errors,
        Model model, RedirectAttributes redirectAttributes) {

        calendarSettingsValidator.validate(calendarSettings, errors);

        if (errors.hasErrors()) {
            return "settings/calendar_settings_form";
        }

        settingsService.save(calendarSettings);

        calendarSyncService.checkCalendarSyncSettings();

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/settings";
    }
}
