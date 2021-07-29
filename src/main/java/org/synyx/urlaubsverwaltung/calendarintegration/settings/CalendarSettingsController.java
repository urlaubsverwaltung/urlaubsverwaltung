package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsValidator.validateCalendarSettings;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/calendar/settings")
public class CalendarSettingsController {

    private final CalendarSettingsService calendarSettingsService;
    private final List<CalendarProvider> calendarProviders;

    public CalendarSettingsController(CalendarSettingsService calendarSettingsService, List<CalendarProvider> calendarProviders) {
        this.calendarSettingsService = calendarSettingsService;
        this.calendarProviders = calendarProviders;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getCalendarSettings(HttpServletRequest request,  Model model) {

        final CalendarSettingsDto settingsDto = calendarSettingsService.getSettingsDto(request);
        model.addAttribute("calendarSettings", settingsDto);
        model.addAttribute("calendarProviders", getAllProviders());

        return "calendarintegration/calendar_settings";
    }


    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveCalendarSettings(@ModelAttribute("calendarSettings") CalendarSettingsDto calendarSettingsDto,
                                       @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                       HttpServletRequest request, Model model, Errors errors) {

        final Errors validationErrors = validateCalendarSettings(calendarSettingsDto, errors);

        if(validationErrors.hasErrors()) {

            model.addAttribute("calendarSettings", calendarSettingsDto);
            model.addAttribute("errors", validationErrors);
        } else {

            final CalendarSettingsDto newCalendarSettingsDto = calendarSettingsService.save(request, calendarSettingsDto);
            model.addAttribute("calendarSettings", newCalendarSettingsDto);
            model.addAttribute("success", true);
        }

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        return "calendarintegration/calendar_settings";
    }

    public List<String> getAllProviders() {
        return calendarProviders.stream()
            .map(provider -> provider.getClass().getSimpleName())
            .sorted(reverseOrder())
            .collect(toList());
    }
}
