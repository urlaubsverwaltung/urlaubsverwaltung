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
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.time.DayOfWeek;
import java.util.List;
import java.util.TimeZone;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/working-time")
public class SettingsWorkingTimeViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsWorkingTimeValidator settingsValidator;

    @Autowired
    public SettingsWorkingTimeViewController(SettingsService settingsService, SettingsWorkingTimeValidator settingsValidator) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsWorkingTimeDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/public-holidays/settings_working_time";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsWorkingTimeDto settingsDto, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/public-holidays/settings_working_time";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/working-time";
    }

    private void fillModel(Model model, SettingsWorkingTimeDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("availableTimezones", List.of(TimeZone.getAvailableIDs()));
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("dayLengthTypes", DayLength.values());
        model.addAttribute("weekDays", DayOfWeek.values());
    }

    private SettingsWorkingTimeDto settingsToDto(Settings settings) {
        // TODO use DTOs for settings
        final SettingsWorkingTimeDto dto = new SettingsWorkingTimeDto();
        dto.setId(settings.getId());
        dto.setWorkingTimeSettings(settings.getWorkingTimeSettings());
        dto.setOvertimeSettings(settings.getOvertimeSettings());
        dto.setTimeSettings(settings.getTimeSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsWorkingTimeDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setWorkingTimeSettings(settingsDto.getWorkingTimeSettings());
        settings.setOvertimeSettings(settingsDto.getOvertimeSettings());
        settings.setTimeSettings(settingsDto.getTimeSettings());
        return settings;
    }
}
