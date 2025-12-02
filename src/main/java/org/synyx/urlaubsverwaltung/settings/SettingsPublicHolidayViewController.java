package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/public-holidays")
public class SettingsPublicHolidayViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final SettingsPublicHolidayValidator settingsValidator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public SettingsPublicHolidayViewController(
        SettingsService settingsService,
        SettingsPublicHolidayValidator settingsValidator,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.settingsService = settingsService;
        this.settingsValidator = settingsValidator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsPublicHolidayDto settingsDto = settingsToDto(settings);

        fillModel(model, settingsDto);

        return "settings/public-holidays/settings_public_holidays";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(
        @Valid @ModelAttribute("settings") SettingsPublicHolidayDto settingsDto, Errors errors,
        Model model, RedirectAttributes redirectAttributes
    ) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/public-holidays/settings_public_holidays";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(settings);
        applicationEventPublisher.publishEvent(new WorkingDurationForChristmasEveUpdatedEvent(settings.getPublicHolidaysSettings().getWorkingDurationForChristmasEve()));
        applicationEventPublisher.publishEvent(new WorkingDurationForNewYearsEveUpdatedEvent(settings.getPublicHolidaysSettings().getWorkingDurationForNewYearsEve()));

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/public-holidays";
    }

    private void fillModel(Model model, SettingsPublicHolidayDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("dayLengthTypes", DayLength.values());
    }

    private SettingsPublicHolidayDto settingsToDto(Settings settings) {
        final SettingsPublicHolidayDto dto = new SettingsPublicHolidayDto();
        dto.setId(settings.getId());
        dto.setPublicHolidaysSettings(settings.getPublicHolidaysSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsPublicHolidayDto settingsDto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(settingsDto.getId());
        settings.setPublicHolidaysSettings(settingsDto.getPublicHolidaysSettings());
        return settings;
    }
}
