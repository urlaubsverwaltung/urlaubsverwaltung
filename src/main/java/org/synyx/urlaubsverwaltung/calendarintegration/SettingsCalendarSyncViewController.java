package org.synyx.urlaubsverwaltung.calendarintegration;

import de.focus_shift.launchpad.api.HasLaunchpad;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.TimeZone;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/calendar-sync")
public class SettingsCalendarSyncViewController implements HasLaunchpad {

    private final CalendarSettingsService calendarSettingsService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsCalendarSyncValidator calendarSyncValidator;

    @Autowired
    SettingsCalendarSyncViewController(
        CalendarSettingsService calendarSettingsService,
        List<CalendarProvider> calendarProviders,
        SettingsCalendarSyncValidator calendarSyncValidator
    ) {
        this.calendarSettingsService = calendarSettingsService;
        this.calendarProviders = calendarProviders;
        this.calendarSyncValidator = calendarSyncValidator;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final CalendarSettings settings = calendarSettingsService.getCalendarSettings();
        final SettingsCalendarSyncDto settingsDto = settingsToDto(settings);
        fillModel(model, settingsDto, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/calendar/settings_calendar_sync";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsCalendarSyncDto calendarSettingsDto, Errors errors,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        calendarSyncValidator.validate(calendarSettingsDto, errors);

        if (errors.hasErrors()) {

            final String authorizedRedirectUrl = getAuthorizedRedirectUrl(request.getRequestURL().toString(), "oautherrors");

            fillModel(model, calendarSettingsDto, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/calendar/settings_calendar_sync";
        }

        final CalendarSettings calendarSettings = settingsDtoToSettings(calendarSettingsDto);
        calendarSettingsService.save(processGoogleRefreshToken(calendarSettings));

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings/calendar-sync";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings/calendar-sync", redirectPath);
    }

    private void fillModel(Model model, SettingsCalendarSyncDto settingsDto, String authorizedRedirectUrl) {

        model.addAttribute("settings", settingsDto);
        model.addAttribute("availableTimezones", List.of(TimeZone.getAvailableIDs()));
        model.addAttribute("authorizedRedirectUrl", authorizedRedirectUrl);

        final List<String> providers = calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(reverseOrder())
            .toList();

        model.addAttribute("providers", concat(of("NoSyncProvider"), providers.stream()).toList());
    }

    private SettingsCalendarSyncDto settingsToDto(CalendarSettings calendarSettings) {
        // TODO use DTOs for settings
        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setId(calendarSettings.getId());
        dto.setCalendarSettings(calendarSettings);
        return dto;
    }

    private CalendarSettings settingsDtoToSettings(SettingsCalendarSyncDto dto) {
        final CalendarSettings calendarSettings = calendarSettingsService.getCalendarSettings();
        calendarSettings.setId(dto.getId());
        calendarSettings.setProvider(dto.getCalendarSettings().getProvider());
        calendarSettings.setGoogleCalendarSettings(dto.getCalendarSettings().getGoogleCalendarSettings());
        return calendarSettings;
    }

    private CalendarSettings processGoogleRefreshToken(CalendarSettings settingsUpdate) {

        final GoogleCalendarSettings storedGoogleSettings = calendarSettingsService.getCalendarSettings().getGoogleCalendarSettings();

        if (storedGoogleSettings != null && settingsUpdate != null) {
            final GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getGoogleCalendarSettings();
            updateGoogleSettings.setRefreshToken(storedGoogleSettings.getRefreshToken());

            if (refreshTokenGotInvalid(storedGoogleSettings, updateGoogleSettings)) {
                // refresh token is invalid if settings changed
                updateGoogleSettings.setRefreshToken(null);
            }
        }
        return settingsUpdate;
    }

    private boolean refreshTokenGotInvalid(GoogleCalendarSettings oldSettings, GoogleCalendarSettings newSettings) {
        if (oldSettings.getClientSecret() == null || oldSettings.getClientId() == null || oldSettings.getCalendarId() == null) {
            return true;
        }

        return !oldSettings.equals(newSettings);
    }

    private boolean shouldShowOAuthError(String googleOAuthError, CalendarSettings calendarSettings) {
        return googleOAuthError != null
            && !googleOAuthError.isEmpty()
            && calendarSettings.getGoogleCalendarSettings().getRefreshToken() == null;
    }
}
