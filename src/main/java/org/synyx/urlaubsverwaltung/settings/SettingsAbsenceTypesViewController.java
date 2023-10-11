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
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;

import java.util.List;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.settings.AbsenceTypeSettingsDtoMapper.mapToAbsenceTypeItemSettingDto;
import static org.synyx.urlaubsverwaltung.settings.SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsItems;

@Controller
@RequestMapping("/web/settings/absence-types")
public class SettingsAbsenceTypesViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final VacationTypeService vacationTypeService;
    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    @Autowired
    public SettingsAbsenceTypesViewController(SettingsService settingsService,
                                              VacationTypeService vacationTypeService,
                                              SpecialLeaveSettingsService specialLeaveService) {
        this.settingsService = settingsService;
        this.vacationTypeService = vacationTypeService;
        this.specialLeaveSettingsService = specialLeaveService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(Model model) {

        final Settings settings = settingsService.getSettings();
        final SettingsAbsenceTypesDto dto = settingsToDto(settings);

        model.addAttribute("settings", dto);

        return "settings/absence-types/settings_absence_types";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsAbsenceTypesDto settingsDto, Errors errors,
                                Model model, RedirectAttributes redirectAttributes) {

        if (errors.hasErrors()) {
            model.addAttribute("settings", settingsDto);
            model.addAttribute("errors", errors);
            return "settings/absence-types/settings_absence_types";
        }

        final List<VacationTypeUpdate> vacationTypeUpdates = settingsDto.getAbsenceTypeSettings().getItems()
            .stream()
            .map(SettingsAbsenceTypesViewController::absenceTypeDtoToVacationTypeUpdate)
            .toList();
        vacationTypeService.updateVacationTypes(vacationTypeUpdates);

        final SpecialLeaveSettingsDto specialLeaveSettingsDto = settingsDto.getSpecialLeaveSettings();
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = mapToSpecialLeaveSettingsItems(specialLeaveSettingsDto.getSpecialLeaveSettingsItems());
        specialLeaveSettingsService.saveAll(specialLeaveSettingsItems);

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/absence-types";
    }

    private SettingsAbsenceTypesDto settingsToDto(Settings settings) {
        final SettingsAbsenceTypesDto dto = new SettingsAbsenceTypesDto();
        dto.setId(settings.getId());
        dto.setAbsenceTypeSettings(absenceTypeItemSettingDto());
        dto.setSpecialLeaveSettings(getSpecialLeaveSettingsDto());
        return dto;
    }

    private AbsenceTypeSettingsDto absenceTypeItemSettingDto() {
        final List<VacationType> allVacationTypes = vacationTypeService.getAllVacationTypes();
        return mapToAbsenceTypeItemSettingDto(allVacationTypes);
    }

    private SpecialLeaveSettingsDto getSpecialLeaveSettingsDto() {
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = specialLeaveSettingsService.getSpecialLeaveSettings();
        return SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsDto(specialLeaveSettingsItems);
    }

    private static VacationTypeUpdate absenceTypeDtoToVacationTypeUpdate(AbsenceTypeSettingsItemDto absenceTypeSettingsItemDto) {
        return new VacationTypeUpdate(
            absenceTypeSettingsItemDto.getId(),
            absenceTypeSettingsItemDto.isActive(),
            absenceTypeSettingsItemDto.isRequiresApprovalToApply(),
            absenceTypeSettingsItemDto.isRequiresApprovalToCancel(),
            absenceTypeSettingsItemDto.getColor(),
            absenceTypeSettingsItemDto.isVisibleToEveryone());
    }
}
