package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/absences")
public class SettingsAbsencesViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsAbsencesValidator validator;

    @Autowired
    public SettingsAbsencesViewController(SettingsService settingsService, SettingsAbsencesValidator validator) {
        this.settingsService = settingsService;
        this.validator = validator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsAbsencesDto dto = settingsToDto(settings);

        model.addAttribute("settings", dto);

        return "settings/absences/settings_absences";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsAbsencesDto settingsDto, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {

        validator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            model.addAttribute("settings", settingsDto);
            model.addAttribute("errors", errors);
            return "settings/absences/settings_absences";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/absences";
    }

    private SettingsAbsencesDto settingsToDto(Settings settings) {
        // TODO use DTOs for settings
        final SettingsAbsencesDto dto = new SettingsAbsencesDto();
        dto.setId(settings.getId());
        dto.setApplicationSettings(settings.getApplicationSettings());
        dto.setAccountSettings(settings.getAccountSettings());
        dto.setSickNoteSettings(settings.getSickNoteSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsAbsencesDto dto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(dto.getId());
        settings.setApplicationSettings(dto.getApplicationSettings());
        settings.setAccountSettings(dto.getAccountSettings());
        settings.setSickNoteSettings(dto.getSickNoteSettings());
        return settings;
    }
}
