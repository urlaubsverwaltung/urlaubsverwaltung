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

import java.util.List;
import java.util.TimeZone;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/calendar")
public class SettingsCalendarViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsCalendarValidator settingsValidator;

    @Autowired
    public SettingsCalendarViewController(
        SettingsService settingsService,
        SettingsCalendarValidator settingsValidator
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsCalendarDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/calendar/settings_calendar";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsCalendarDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/calendar/settings_calendar";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/calendar";
    }

    private void fillModel(Model model, SettingsCalendarDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("availableTimezones", List.of(TimeZone.getAvailableIDs()));
    }

    private SettingsCalendarDto settingsToDto(Settings settings) {
        final SettingsCalendarDto dto = new SettingsCalendarDto();
        dto.setId(settings.getId());
        dto.setTimeSettings(settings.getTimeSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsCalendarDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setTimeSettings(settingsDto.getTimeSettings());
        return settings;
    }
}
