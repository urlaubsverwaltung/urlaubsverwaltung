package org.synyx.urlaubsverwaltung.settings;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsItem;
import org.synyx.urlaubsverwaltung.application.specialleave.SpecialLeaveSettingsService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Clock;
import java.time.DayOfWeek;
import java.util.List;
import java.util.TimeZone;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;
import static org.synyx.urlaubsverwaltung.settings.AbsenceTypeSettingsDtoMapper.mapToAbsenceTypeItemSettingDto;
import static org.synyx.urlaubsverwaltung.settings.SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsItems;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final VacationTypeService vacationTypeService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsValidator settingsValidator;
    private final Clock clock;
    private final SpecialLeaveSettingsService specialLeaveSettingsService;

    @Autowired
    public SettingsViewController(SettingsService settingsService, VacationTypeService vacationTypeService, List<CalendarProvider> calendarProviders,
                                  SettingsValidator settingsValidator, Clock clock, SpecialLeaveSettingsService specialLeaveService) {
        this.settingsService = settingsService;
        this.vacationTypeService = vacationTypeService;
        this.calendarProviders = calendarProviders;
        this.settingsValidator = settingsValidator;
        this.clock = clock;
        this.specialLeaveSettingsService = specialLeaveService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final Settings settings = settingsService.getSettings();
        final SettingsDto settingsDto = settingsToDto(settings);
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());
        settingsDto.setSpecialLeaveSettings(getSpecialLeaveSettingsDto());
        fillModel(model, settingsDto, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/settings_form";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsDto settingsDto, Errors errors,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsValidator.validate(settings, errors);

        if (errors.hasErrors()) {

            final StringBuffer requestURL = request.getRequestURL();
            final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL.toString(), "oautherrors");

            fillModel(model, settingsDto, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/settings_form";
        }

        settingsService.save(processGoogleRefreshToken(settings));

        final List<VacationTypeUpdate> vacationTypeUpdates = settingsDto.getAbsenceTypeSettings().getItems()
            .stream()
            .map(SettingsViewController::absenceTypeDtoToVacationTypeUpdate)
            .collect(toList());
        vacationTypeService.updateVacationTypes(vacationTypeUpdates);

        final SpecialLeaveSettingsDto specialLeaveSettingsDto = settingsDto.getSpecialLeaveSettings();
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = mapToSpecialLeaveSettingsItems(specialLeaveSettingsDto.getSpecialLeaveSettingsItems());
        specialLeaveSettingsService.saveAll(specialLeaveSettingsItems);

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings", redirectPath);
    }

    private SpecialLeaveSettingsDto getSpecialLeaveSettingsDto() {
        final List<SpecialLeaveSettingsItem> specialLeaveSettingsItems = specialLeaveSettingsService.getSpecialLeaveSettings();
        return SpecialLeaveSettingsDtoMapper.mapToSpecialLeaveSettingsDto(specialLeaveSettingsItems);
    }

    private void fillModel(Model model, SettingsDto settingsDto, String authorizedRedirectUrl) {
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());
        settingsDto.setSpecialLeaveSettings(getSpecialLeaveSettingsDto());

        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.federalStatesTypesByCountry());
        model.addAttribute("dayLengthTypes", DayLength.values());
        model.addAttribute("weekDays", DayOfWeek.values());

        final List<String> providers = calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(reverseOrder())
            .collect(toList());
        model.addAttribute("providers", providers);

        model.addAttribute("availableTimezones", List.of(TimeZone.getAvailableIDs()));

        if (settingsDto.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId() == null) {
            settingsDto.getCalendarSettings().getExchangeCalendarSettings().setTimeZoneId(clock.getZone().getId());
        }

        model.addAttribute("authorizedRedirectUrl", authorizedRedirectUrl);
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
        settingsDto.setAvatarSettings(settings.getAvatarSettings());
        settingsDto.setCalendarSettings(settings.getCalendarSettings());

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
        settings.setAvatarSettings(settingsDto.getAvatarSettings());
        settings.setCalendarSettings(settingsDto.getCalendarSettings());
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
            absenceTypeSettingsItemDto.isRequiresApprovalToApply(),
            absenceTypeSettingsItemDto.isRequiresApprovalToCancel(),
            absenceTypeSettingsItemDto.getColor(),
            absenceTypeSettingsItemDto.isVisibleToEveryone());
    }

    private Settings processGoogleRefreshToken(Settings settingsUpdate) {

        final GoogleCalendarSettings storedGoogleSettings = settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();
        final GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getCalendarSettings().getGoogleCalendarSettings();

        if (storedGoogleSettings != null) {
            updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

            if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
                // refresh token is invalid if settings changed
                updateGoogleSettings.setRefreshToken(null);
            }
        }
        return settingsUpdate;
    }

    private boolean refreshTokenGotInvalid(GoogleCalendarSettings oldSettings, GoogleCalendarSettings newSettings) {
        if (oldSettings.getClientSecret() == null
            || oldSettings.getClientId() == null
            || oldSettings.getCalendarId() == null) {
            return true;
        }

        return !oldSettings.equals(newSettings);
    }

    private boolean shouldShowOAuthError(String googleOAuthError, Settings settings) {
        return googleOAuthError != null
            && !googleOAuthError.isEmpty()
            && settings.getCalendarSettings().getGoogleCalendarSettings().getRefreshToken() == null;
    }
}
