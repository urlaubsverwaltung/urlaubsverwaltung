package org.synyx.urlaubsverwaltung.absence.settings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/time/settings")
public class TimeSettingsController {

    private final TimeSettingsService timeSettingsService;

    public TimeSettingsController(TimeSettingsService timeSettingsService) {
        this.timeSettingsService = timeSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getTimeSettings( Model model) {

        TimeSettingsDto settingsDto = timeSettingsService.getSettingsDto();
        model.addAttribute("timeSettings", settingsDto);

        return "absences/absences_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveTimeSettings(@ModelAttribute("timeSettings") TimeSettingsDto timeSettingsDto, Model model) {

        timeSettingsService.save(timeSettingsDto);
        model.addAttribute("timeSettings", timeSettingsDto);

        return "absences/absences_settings";
    }
}
