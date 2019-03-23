package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.core.sicknote.statistics.SickNoteStatisticsService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;


/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web")
public class SickNoteStatisticsController {

    private final SickNoteStatisticsService statisticsService;

    @Autowired
    public SickNoteStatisticsController(SickNoteStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/sicknote/statistics")
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear,
        Model model) {

        Integer year = requestedYear == null ? DateMidnight.now().getYear() : requestedYear;

        SickNoteStatistics statistics = statisticsService.createStatistics(year);

        model.addAttribute("statistics", statistics);

        return "sicknote/sick_notes_statistics";
    }
}
