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
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import javax.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.settings.AbsenceTypeSettingsDtoMapper.mapToAbsenceTypeItemSettingDto;
import static org.synyx.urlaubsverwaltung.settings.SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsItems;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final SettingsService settingsService;
    private final VacationTypeService vacationTypeService;
    private final SettingsValidator settingsValidator;
    private final String applicationVersion;
    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    @Autowired
    public SettingsViewController(SettingsService settingsService, VacationTypeService vacationTypeService,
                                  SettingsValidator settingsValidator, @Value("${info.app.version}") String applicationVersion, SpecialLeaveSettingsService specialLeaveService) {
        this.settingsService = settingsService;
        this.vacationTypeService = vacationTypeService;
        this.settingsValidator = settingsValidator;
        this.applicationVersion = applicationVersion;
        this.specialLeaveSettingsService = specialLeaveService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsDto settingsDto = settingsToDto(settings);
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());
        settingsDto.setSpecialLeaveSettings(getSpecialLeaveSettingsDto());
        fillModel(model, settings);

        return "settings/settings_form";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsDto settingsDto, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {

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

        final SpecialLeaveSettingsDto specialLeaveSettingsDto = settingsDto.getSpecialLeaveSettings();
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = mapToSpecialLeaveSettingsItems(specialLeaveSettingsDto.getSpecialLeaveSettingsItems());
        specialLeaveSettingsService.saveAll(specialLeaveSettingsItems);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }

    private void fillModel(Model model, Settings settings) {
        final SettingsDto settingsDto = settingsToDto(settings);
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());
        settingsDto.setSpecialLeaveSettings(getSpecialLeaveSettingsDto());

        fillModel(model, settingsDto);
    }

    private SpecialLeaveSettingsDto getSpecialLeaveSettingsDto() {
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = specialLeaveSettingsService.getSpecialLeaveSettings();
        return SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsDto(specialLeaveSettingsItems);
    }

    private void fillModel(Model model, SettingsDto settingsDto) {
        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("dayLengthTypes", DayLength.values());
        model.addAttribute("weekDays", DayOfWeek.values());
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
        final List<VacationType> allVacationTypes = vacationTypeService.getAllVacationTypes();
        return mapToAbsenceTypeItemSettingDto(allVacationTypes);
    }

    private static VacationTypeUpdate absenceTypeDtoToVacationTypeUpdate(AbsenceTypeSettingsItemDto absenceTypeSettingsItemDto) {
        return new VacationTypeUpdate(
            absenceTypeSettingsItemDto.getId(),
            absenceTypeSettingsItemDto.isActive(),
            absenceTypeSettingsItemDto.isRequiresApproval(),
            absenceTypeSettingsItemDto.getColor(),
            absenceTypeSettingsItemDto.isVisibleToEveryone());
    }
}
