package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/workingtime/settings")
public class WorkingTimeSettingsController {

    private final WorkingTimeSettingsService workingTimeSettingsService;

    public WorkingTimeSettingsController(WorkingTimeSettingsService workingTimeSettingsService) {
        this.workingTimeSettingsService = workingTimeSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getWorkingTimeSettings( Model model) {

        WorkingTimeSettingsDto settingsDto = workingTimeSettingsService.getSettingsDto();
        model.addAttribute("workingTimeSettings", settingsDto);

        return "workingtime/workingtime_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveWorkingTimeSettings(@ModelAttribute("workingTimeSettings") WorkingTimeSettingsDto workingTimeSettingsDto,
                                          Model model) {

        workingTimeSettingsService.save(workingTimeSettingsDto);
        model.addAttribute("workingTimeSettings", workingTimeSettingsDto);
        model.addAttribute("success", true);

        return "workingtime/workingtime_settings";
    }
}
