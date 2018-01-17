package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.GoogleCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Controller
@RequestMapping("/web/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final CalendarSyncService calendarSyncService;
    private final List<CalendarProvider> calendarProviders;
    private final MailService mailService;
    private final SettingsValidator settingsValidator;

    @Autowired
    public SettingsController(SettingsService settingsService,
                              CalendarSyncService calendarSyncService,
                              List<CalendarProvider> calendarProviders,
                              MailService mailService,
                              SettingsValidator settingsValidator) {
        this.settingsService = settingsService;
        this.calendarSyncService = calendarSyncService;
        this.calendarProviders = calendarProviders;
        this.mailService = mailService;
        this.settingsValidator = settingsValidator;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping
    public String settingsDetails(Model model,
                                  @RequestParam(value = ControllerConstants.OAUTH_ERROR_ATTRIBUTE, required = false) String googleOAuthError,
                                  HttpServletRequest request) {

        String authorizedRedirectUrl = getAuthorizedRedirectUrl(
                request.getRequestURL().toString(), ControllerConstants.OATUH_REDIRECT_REL);

        Settings settings = settingsService.getSettings();

        fillModel(model, settings, authorizedRedirectUrl);

        if (shouldShowOAuthError(googleOAuthError, settings)) {
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, googleOAuthError);
            model.addAttribute(ControllerConstants.OAUTH_ERROR_ATTRIBUTE, googleOAuthError);
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
                request.getRequestURL().toString(), ControllerConstants.OATUH_REDIRECT_REL);

        settingsValidator.validate(settings, errors);

        if (errors.hasErrors()) {
            fillModel(model, settings, authorizedRedirectUrl);

            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);

            return "settings/settings_form";
        }

        settingsService.save(processGoogleRefreshToken(settings));
        mailService.sendSuccessfullyUpdatedSettingsNotification(settings);

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
