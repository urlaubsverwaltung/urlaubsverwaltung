package org.synyx.urlaubsverwaltung.settings;

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
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;

import java.time.Clock;
import java.util.List;
import java.util.TimeZone;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/settings/calendar-sync")
public class SettingsCalendarSyncViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsCalendarSyncValidator settingsValidator;
    private final Clock clock;

    @Autowired
    public SettingsCalendarSyncViewController(SettingsService settingsService,
                                              List<CalendarProvider> calendarProviders,
                                              SettingsCalendarSyncValidator settingsValidator,
                                              Clock clock) {
        this.settingsService = settingsService;
        this.calendarProviders = calendarProviders;
        this.settingsValidator = settingsValidator;
        this.clock = clock;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String settingsDetails(@RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request, Model model) {

        final String requestURL = request.getRequestURL().toString();
        final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL, "/google-api-handshake");

        final Settings settings = settingsService.getSettings();
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
    public String settingsSaved(@Valid @ModelAttribute("settings") SettingsCalendarSyncDto settingsDto, Errors errors,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        settingsValidator.validate(settingsDto, errors);

        if (errors.hasErrors()) {

            final StringBuffer requestURL = request.getRequestURL();
            final String authorizedRedirectUrl = getAuthorizedRedirectUrl(requestURL.toString(), "oautherrors");

            fillModel(model, settingsDto, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/calendar/settings_calendar_sync";
        }

        final Settings settings = settingsDtoToSettings(settingsDto);
        settingsService.save(processGoogleRefreshToken(settings));

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
            .collect(toList());
        model.addAttribute("providers", providers);

        final ExchangeCalendarSettings exchangeCalendarSettings = settingsDto.getCalendarSettings().getExchangeCalendarSettings();
        if (exchangeCalendarSettings.getTimeZoneId() == null) {
            exchangeCalendarSettings.setTimeZoneId(clock.getZone().getId());
        }
    }

    private SettingsCalendarSyncDto settingsToDto(Settings settings) {
        // TODO use DTOs for settings
        final SettingsCalendarSyncDto dto = new SettingsCalendarSyncDto();
        dto.setId(settings.getId());
        dto.setCalendarSettings(settings.getCalendarSettings());
        return dto;
    }

    private Settings settingsDtoToSettings(SettingsCalendarSyncDto dto) {
        final Settings settings = settingsService.getSettings();
        settings.setId(dto.getId());
        settings.setCalendarSettings(dto.getCalendarSettings());
        return settings;
    }

    private Settings processGoogleRefreshToken(Settings settingsUpdate) {

        final GoogleCalendarSettings storedGoogleSettings = settingsService.getSettings().getCalendarSettings().getGoogleCalendarSettings();
        final CalendarSettings updatedCalendarSettings = settingsUpdate.getCalendarSettings();

        if (storedGoogleSettings != null && updatedCalendarSettings != null) {
            final GoogleCalendarSettings updateGoogleSettings = updatedCalendarSettings.getGoogleCalendarSettings();
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
