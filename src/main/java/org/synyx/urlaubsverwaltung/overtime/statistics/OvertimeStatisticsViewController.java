package org.synyx.urlaubsverwaltung.overtime.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

/**
 * Controller for the company wide overtime statistics.
 */
@Controller
@RequestMapping("/web/overtime/statistics")
@PreAuthorize(IS_BOSS_OR_OFFICE)
class OvertimeStatisticsViewController implements HasLaunchpad {

    private final SettingsService settingsService;
    private final Clock clock;

    OvertimeStatisticsViewController(SettingsService settingsService, Clock clock) {
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @GetMapping
    public String overtimeStatistics(@RequestParam(value = "year", required = false) Optional<String> requestedYear, Model model) {

        if (!settingsService.getSettings().getOvertimeSettings().isOvertimeActive()) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final Year currentYear = Year.now(clock);
        final Year selectedYear = requestedYear.flatMap(OvertimeStatisticsViewController::toYear).orElse(currentYear);

        model.addAttribute("selectedYear", selectedYear.getValue());
        model.addAttribute("currentYear", currentYear.getValue());

        return "overtime/overtime_statistics";
    }

    /**
     * The year is part of a shareable url, therefore an unusable value must not end in an error page.
     */
    private static Optional<Year> toYear(String value) {
        try {
            return Optional.of(Year.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
