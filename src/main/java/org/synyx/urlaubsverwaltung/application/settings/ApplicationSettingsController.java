package org.synyx.urlaubsverwaltung.application.settings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.application.settings.ApplicationSettingsValidator.validateApplicationSettings;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/application/settings")
public class ApplicationSettingsController {

    private final ApplicationSettingsService applicationSettingsService;

    public ApplicationSettingsController(ApplicationSettingsService applicationSettingsService) {
        this.applicationSettingsService = applicationSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getApplicationSettings(Model model) {

        ApplicationSettingsDto settingsDto = applicationSettingsService.getSettingsDto();
        model.addAttribute("applicationSettings", settingsDto);

        return "application/app_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveApplicationSettings(@ModelAttribute("applicationSettings") ApplicationSettingsDto applicationSettingsDto,
                                          Model model, Errors errors) {

        Errors validationErrors = validateApplicationSettings(applicationSettingsDto, errors);

        if(validationErrors.hasErrors()) {
            model.addAttribute("errors", validationErrors);
        } else {
            applicationSettingsService.save(applicationSettingsDto);
            model.addAttribute("success", true);
        }

        model.addAttribute("applicationSettings", applicationSettingsDto);
        return "application/app_settings";
    }
}
