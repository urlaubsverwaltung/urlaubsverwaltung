package org.synyx.urlaubsverwaltung.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final SettingsService settingsService;
    private final VacationTypeService vacationTypeService;
    private final SettingsValidator settingsValidator;
    private final String applicationVersion;

    @Autowired
    public SettingsViewController(SettingsService settingsService, VacationTypeService vacationTypeService,
                                  SettingsValidator settingsValidator, @Value("${info.app.version}") String applicationVersion) {
        this.settingsService = settingsService;
        this.vacationTypeService = vacationTypeService;
        this.settingsValidator = settingsValidator;
        this.applicationVersion = applicationVersion;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        fillModel(model, settings);

        return "settings/settings_form";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@ModelAttribute("settings") SettingsDto settingsDto, Errors errors, Model model, RedirectAttributes redirectAttributes) {

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsValidator.validate(settings, errors);

        if (errors.hasErrors()) {
            fillModel(model, settingsDto);
            model.addAttribute("errors", errors);
            return "settings/settings_form";
        }

        settingsService.save(settings);

        final List<VacationTypeUpdate> vacationTypeUpdates = settingsDto.getAbsenceTypeSettings().getItems()
            .stream()
            .map(SettingsViewController::absenceTypeDtoToVacationTypeUpdate)
            .collect(toList());
        vacationTypeService.updateVacationTypes(vacationTypeUpdates);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }

    private void fillModel(Model model, Settings settings) {
        final SettingsDto settingsDto = settingsToDto(settings);
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());

        fillModel(model, settingsDto);
    }

    private void fillModel(Model model, SettingsDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());
        model.addAttribute("version", applicationVersion);
    }

    private SettingsDto settingsToDto(Settings settings) {
        // TODO use DTOs for settings
        final SettingsDto settingsDto = new SettingsDto();
        settingsDto.setId(settings.getId());
        settingsDto.setApplicationSettings(settings.getApplicationSettings());
        settingsDto.setAccountSettings(settings.getAccountSettings());
        settingsDto.setWorkingTimeSettings(settings.getWorkingTimeSettings());
        settingsDto.setOvertimeSettings(settings.getOvertimeSettings());
        settingsDto.setTimeSettings(settings.getTimeSettings());
        settingsDto.setSickNoteSettings(settings.getSickNoteSettings());
        return settingsDto;
    }

    private Settings settingsDtoToSettings(SettingsDto settingsDto) {
        final Settings settings = new Settings();
        settings.setId(settingsDto.getId());
        settings.setApplicationSettings(settingsDto.getApplicationSettings());
        settings.setAccountSettings(settingsDto.getAccountSettings());
        settings.setWorkingTimeSettings(settingsDto.getWorkingTimeSettings());
        settings.setOvertimeSettings(settingsDto.getOvertimeSettings());
        settings.setTimeSettings(settingsDto.getTimeSettings());
        settings.setSickNoteSettings(settingsDto.getSickNoteSettings());
        return settings;
    }

    private AbsenceTypeSettingsDto absenceTypeItemSettingDto() {
        final List<AbsenceTypeSettingsItemDto> absenceTypeDtos = vacationTypeService.getAllVacationTypes()
            .stream()
            .map(this::vacationTypeToDto)
            .collect(toList());

        final AbsenceTypeSettingsDto absenceTypeSettingsDto = new AbsenceTypeSettingsDto();
        absenceTypeSettingsDto.setItems(absenceTypeDtos);

        return absenceTypeSettingsDto;
    }

    private AbsenceTypeSettingsItemDto vacationTypeToDto(VacationType vacationType) {
        return AbsenceTypeSettingsItemDto.builder()
            .setId(vacationType.getId())
            .setMessageKey(vacationType.getMessageKey())
            .setCategory(vacationType.getCategory())
            .setActive(vacationType.isActive())
            .setRequiresApproval(vacationType.isRequiresApproval())
            .build();
    }

    private static VacationTypeUpdate absenceTypeDtoToVacationTypeUpdate(AbsenceTypeSettingsItemDto absenceTypeSettingsItemDto) {
        return new VacationTypeUpdate(
            absenceTypeSettingsItemDto.getId(),
            absenceTypeSettingsItemDto.isActive(),
            absenceTypeSettingsItemDto.isRequiresApproval()
        );
    }
}
