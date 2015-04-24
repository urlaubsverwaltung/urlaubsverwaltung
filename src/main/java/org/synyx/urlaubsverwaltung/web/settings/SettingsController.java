package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.validator.SettingsValidator;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Controller
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SettingsValidator settingsValidator;

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public String settingsDetails(Model model) {

        if (sessionService.isOffice()) {
            model.addAttribute("settings", settingsService.getSettings());
            model.addAttribute("federalStateTypes", FederalState.values());
            model.addAttribute("dayLengthTypes", DayLength.values());

            return "settings/settings_form";
        }

        return ControllerConstants.ERROR_JSP;
    }


    @RequestMapping(value = "/settings", method = RequestMethod.PUT)
    public String settingsSaved(@ModelAttribute("settings") Settings settings, Errors errors, Model model) {

        if (sessionService.isOffice()) {
            settingsValidator.validate(settings, errors);

            if (!errors.hasErrors()) {
                settingsService.save(settings);

                return "redirect:/web/settings";
            } else {
                model.addAttribute("settings", settings);
                model.addAttribute("federalStateTypes", FederalState.values());
                model.addAttribute("dayLengthTypes", DayLength.values());

                return "settings/settings_form";
            }
        }

        return ControllerConstants.ERROR_JSP;
    }
}
