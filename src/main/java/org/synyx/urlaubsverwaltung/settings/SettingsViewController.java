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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeUpdate;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeProperties;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.List;
import java.util.TimeZone;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final WorkingTimeProperties workingTimeProperties;
    private final SettingsService settingsService;
    private final VacationTypeService vacationTypeService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsValidator settingsValidator;
    private final Clock clock;
    private final String applicationVersion;

    @Autowired
    public SettingsViewController(WorkingTimeProperties workingTimeProperties,
                                  SettingsService settingsService, VacationTypeService vacationTypeService, List<CalendarProvider> calendarProviders,
                                  SettingsValidator settingsValidator, Clock clock, @Value("${info.app.version}") String applicationVersion) {
        this.workingTimeProperties = workingTimeProperties;
        this.settingsService = settingsService;
        this.vacationTypeService = vacationTypeService;
        this.calendarProviders = calendarProviders;
        this.settingsValidator = settingsValidator;
        this.clock = clock;
        this.applicationVersion = applicationVersion;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final Settings settings = settingsService.getSettings();
        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/settings_form";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@ModelAttribute("settings") SettingsDto settingsDto,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                Errors errors, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

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

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings", redirectPath);
    }

    private void fillModel(Model model, Settings settings, String authorizedRedirectUrl) {
        final SettingsDto settingsDto = settingsToDto(settings);
        settingsDto.setAbsenceTypeSettings(absenceTypeItemSettingDto());

        fillModel(model, settingsDto, authorizedRedirectUrl);
    }

    private void fillModel(Model model, SettingsDto settingsDto, String authorizedRedirectUrl) {
        model.addAttribute("defaultWorkingTimeFromSettings", workingTimeProperties.isDefaultWorkingDaysDeactivated());

        model.addAttribute("settings", settingsDto);
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());

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
        settings.setCalendarSettings(settingsDto.getCalendarSettings());
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

    private Settings processGoogleRefreshToken(Settings settingsUpdate) {
        final Settings storedSettings = settingsService.getSettings();

        final GoogleCalendarSettings storedGoogleSettings = storedSettings.getCalendarSettings().getGoogleCalendarSettings();
        final GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getCalendarSettings().getGoogleCalendarSettings();

        updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

        if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
            // refresh token is invalid if settings changed
            updateGoogleSettings.setRefreshToken(null);
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
