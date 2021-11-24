package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Clock;
import java.time.Year;
import java.time.ZonedDateTime;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_OFFICE;

/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web")
public class SickNoteStatisticsViewController {

    private final SickNoteStatisticsService statisticsService;
    private final Clock clock;

    @Autowired
    SickNoteStatisticsViewController(SickNoteStatisticsService statisticsService, Clock clock) {
        this.statisticsService = statisticsService;
        this.clock = clock;
    }

    @PreAuthorize(IS_OFFICE)
    @GetMapping("/sicknote/statistics")
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear, Model model) {

        final Clock clockOfRequestedYear = getClockOfRequestedYear(requestedYear);
        final SickNoteStatistics statistics = statisticsService.createStatistics(clockOfRequestedYear);

        model.addAttribute("statistics", statistics);

        return "sicknote/sick_notes_statistics";
    }

    private Clock getClockOfRequestedYear(Integer requestedYear) {
        if (requestedYear == null) {
            requestedYear = Year.now(clock).getValue();
        }
        return Clock.fixed(ZonedDateTime.now(clock).withYear(requestedYear).toInstant(), clock.getZone());
    }
}
