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

import java.time.DayOfWeek;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/onboarding")
public class SettingsOnboardingViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsOnboardingValidator settingsValidator;

    @Autowired
    public SettingsOnboardingViewController(
        SettingsService settingsService,
        SettingsOnboardingValidator settingsValidator
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsOnboardingDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/onboarding/settings_onboarding";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsOnboardingDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/onboarding/settings_onboarding";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/onboarding";
    }

    private void fillModel(Model model, SettingsOnboardingDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("weekDays", DayOfWeek.values());
    }

    private SettingsOnboardingDto settingsToDto(Settings settings) {
        final SettingsOnboardingDto dto = new SettingsOnboardingDto();
        dto.setId(settings.getId());
        dto.setWorkingTimeSettings(settings.getWorkingTimeSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsOnboardingDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setWorkingTimeSettings(settingsDto.getWorkingTimeSettings());
        return settings;
    }
}
