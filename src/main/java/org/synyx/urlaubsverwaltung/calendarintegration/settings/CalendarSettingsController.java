package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

@Controller
@RequestMapping("/web/calendar/settings")
public class CalendarSettingsController {

    private final CalendarSettingsService calendarSettingsService;

    public CalendarSettingsController(CalendarSettingsService calendarSettingsService) {
        this.calendarSettingsService = calendarSettingsService;
    }

    @GetMapping
    @PreAuthorize(IS_OFFICE)
    public String getCalendarSettings(HttpServletRequest request,  Model model) {

        final CalendarSettingsDto settingsDto = calendarSettingsService.getSettingsDto(request);
        model.addAttribute("calendarSettings", settingsDto);

        return "calendarintegration/calendar_settings";
    }

    @PostMapping
    @PreAuthorize(IS_OFFICE)
    public String saveCalendarSettings(@ModelAttribute("calendarSettings") CalendarSettingsDto calendarSettingsDto,
                                       @RequestParam(value = "googleOAuthButton", required = false) String googleOAuthButton,
                                       HttpServletRequest request,
                                       Model model) {

        final CalendarSettingsDto newCalendarSettingsDto = calendarSettingsService.save(request, calendarSettingsDto);

        if (googleOAuthButton != null) {
            return "redirect:/web/google-api-handshake";
        }

        model.addAttribute("calendarSettings", newCalendarSettingsDto);
        model.addAttribute("success", true);

        return "calendarintegration/calendar_settings";
    }
}
