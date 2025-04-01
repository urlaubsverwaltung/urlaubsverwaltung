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
@RequestMapping("/web/settings/overtime")
public class SettingsOvertimeViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsOvertimeValidator settingsValidator;

    @Autowired
    public SettingsOvertimeViewController(
        SettingsService settingsService,
        SettingsOvertimeValidator settingsValidator
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsOvertimeDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/overtime/settings_overtime";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsOvertimeDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/overtime/settings_overtime";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/overtime";
    }

    private void fillModel(Model model, SettingsOvertimeDto settingsDto) {
        model.addAttribute("settings", settingsDto);
    }

    private SettingsOvertimeDto settingsToDto(Settings settings) {
        final SettingsOvertimeDto dto = new SettingsOvertimeDto();
        dto.setId(settings.getId());
        dto.setOvertimeSettings(settings.getOvertimeSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsOvertimeDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setOvertimeSettings(settingsDto.getOvertimeSettings());
        return settings;
    }
}
