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
@RequestMapping("/web/settings/account")
public class SettingsAccountViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsAccountValidator settingsValidator;

    @Autowired
    public SettingsAccountViewController(
        SettingsService settingsService,
        SettingsAccountValidator settingsValidator
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsAccountDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/account/settings_onboarding";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsAccountDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/account/settings_onboarding";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/account";
    }

    private void fillModel(Model model, SettingsAccountDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("weekDays", DayOfWeek.values());
    }

    private SettingsAccountDto settingsToDto(Settings settings) {
        final SettingsAccountDto dto = new SettingsAccountDto();
        dto.setId(settings.getId());
        dto.setWorkingTimeSettings(settings.getWorkingTimeSettings());
        dto.setAccountSettings(settings.getAccountSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsAccountDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setWorkingTimeSettings(settingsDto.getWorkingTimeSettings());
        settings.setAccountSettings(settingsDto.getAccountSettings());
        return settings;
    }
}
