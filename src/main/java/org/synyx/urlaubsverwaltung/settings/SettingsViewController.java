package org.synyx.urlaubsverwaltung.settings;

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
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/web/settings")
public class SettingsViewController {

    private final SettingsService settingsService;
    private final List<CalendarProvider> calendarProviders;
    private final SettingsValidator settingsValidator;
    private final Clock clock;

    @Autowired
    public SettingsViewController(SettingsService settingsService,
                                  List<CalendarProvider> calendarProviders,
                                  SettingsValidator settingsValidator, Clock clock) {

        this.settingsService = settingsService;
        this.calendarProviders = calendarProviders;
        this.settingsValidator = settingsValidator;
        this.clock = clock;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping
    public String settingsDetails(Model model,
                                  @RequestParam(value = "oautherrors", required = false) String googleOAuthError,
                                  HttpServletRequest request) {

        String authorizedRedirectUrl = getAuthorizedRedirectUrl(
            request.getRequestURL().toString(), "/google-api-handshake");

        Settings settings = settingsService.getSettings();

        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute("errors", googleOAuthError);
            model.addAttribute("oautherrors", googleOAuthError);
        }

        return "settings/settings_form";
    }

    String getAuthorizedRedirectUrl(String requestURL, String redirectPath) {
        return requestURL.replace("/settings", redirectPath);
    }

    private void fillModel(Model model, Settings settings, String authorizedRedirectUrl) {
        model.addAttribute("settings", settings);
        model.addAttribute("federalStateTypes", FederalState.values());
        model.addAttribute("dayLengthTypes", DayLength.values());

        List<String> providers = calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        model.addAttribute("providers", providers);

        model.addAttribute("availableTimezones", Arrays.asList(TimeZone.getAvailableIDs()));

        if (settings.getCalendarSettings().getExchangeCalendarSettings().getTimeZoneId() == null) {
            settings.getCalendarSettings().getExchangeCalendarSettings().setTimeZoneId(clock.getZone().getId());
        }

        model.addAttribute("authorizedRedirectUrl", authorizedRedirectUrl);
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping
    public String settingsSaved(@ModelAttribute("settings") Settings settings,
                                Errors errors,
                                Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                HttpServletRequest request) {

        String authorizedRedirectUrl = getAuthorizedRedirectUrl(
            request.getRequestURL().toString(), "oautherrors");

        settingsValidator.validate(settings, errors);

        if (errors.hasErrors()) {
            fillModel(model, settings, authorizedRedirectUrl);

            model.addAttribute("errors", errors);

            return "settings/settings_form";
        }

        settingsService.save(processGoogleRefreshToken(settings));

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/web/settings";
    }


    private Settings processGoogleRefreshToken(Settings settingsUpdate) {
        Settings storedSettings = settingsService.getSettings();

        GoogleCalendarSettings storedGoogleSettings = storedSettings.getCalendarSettings().getGoogleCalendarSettings();
        GoogleCalendarSettings updateGoogleSettings = settingsUpdate.getCalendarSettings().getGoogleCalendarSettings();

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
