package org.synyx.urlaubsverwaltung.specialleave;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.specialleave.SpecialLeaveSettingsValidator.validateSpecialLeavceSettings;

@Controller
@RequestMapping("/web/specialleave/settings")
public class SpecialLeaveSettingsController {

    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    public SpecialLeaveSettingsController(SpecialLeaveSettingsService specialLeaveSettingsService) {
        this.specialLeaveSettingsService = specialLeaveSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getSpecialLeaveSettings( Model model) {

        SpecialLeaveSettingsDto settingsDtos = specialLeaveSettingsService.getSettingsDto();
        model.addAttribute("specialLeaveSettings", settingsDtos);

        return "specialleave/specialleave_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveSpecialLeaveSettings(@ModelAttribute("specialLeaveSettings") SpecialLeaveSettingsDto specialLeaveSettingsDto,
                                          Model model, Errors errors) {

        validateSpecialLeavceSettings(specialLeaveSettingsDto, errors);

        if (errors.hasErrors()) {
            model.addAttribute("errors", errors);
        } else {
            specialLeaveSettingsService.save(specialLeaveSettingsDto);
            model.addAttribute("success", true);
        }

        model.addAttribute("specialLeaveSettings", specialLeaveSettingsDto);
        return "specialleave/specialleave_settings";
    }
}
