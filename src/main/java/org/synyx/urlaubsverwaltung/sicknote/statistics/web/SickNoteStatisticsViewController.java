package org.synyx.urlaubsverwaltung.sicknote.statistics.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;

import java.time.Clock;
import java.time.ZonedDateTime;


/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web")
public class SickNoteStatisticsViewController {

    private final SickNoteStatisticsService statisticsService;
    private final Clock clock;

    @Autowired
    public SickNoteStatisticsViewController(SickNoteStatisticsService statisticsService, Clock clock) {
        this.statisticsService = statisticsService;
        this.clock = clock;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/statistics")
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear,
                                      Model model) {

        Clock clockOfRequestedYear = getClockOfRequestedYear(requestedYear);
        SickNoteStatistics statistics = statisticsService.createStatistics(clockOfRequestedYear);

        model.addAttribute("statistics", statistics);

        return "sicknote/sick_notes_statistics";
    }

    private Clock getClockOfRequestedYear(Integer requestedYear) {
        return Clock.fixed(ZonedDateTime.now(clock).withYear(requestedYear).toInstant(), clock.getZone());
    }
}
