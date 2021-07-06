package org.synyx.urlaubsverwaltung.overtime.settings;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/overtime/settings")
public class OvertimeSettingsController {

    private final OvertimeSettingsService overtimeSettingsService;

    public OvertimeSettingsController(OvertimeSettingsService overtimeSettingsService) {
        this.overtimeSettingsService = overtimeSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getOvertimeSettings(Model model) {

        OvertimeSettingsDto settingsDto = overtimeSettingsService.getSettingsDto();
        model.addAttribute("overtimeSettings", settingsDto);

        return "overtime/overtime_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveOvertimeSettings(@ModelAttribute("overtimeSettings") OvertimeSettingsDto overtimeSettingsDto,
                                       Model model) {

        overtimeSettingsService.save(overtimeSettingsDto);
        model.addAttribute("overtimeSettings", overtimeSettingsDto);
        model.addAttribute("success", true);

        return "overtime/overtime_settings";
    }
}
