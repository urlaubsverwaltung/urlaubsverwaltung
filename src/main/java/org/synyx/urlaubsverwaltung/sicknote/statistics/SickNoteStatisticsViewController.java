package org.synyx.urlaubsverwaltung.sicknote.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web/sicknote/statistics")
class SickNoteStatisticsViewController implements HasLaunchpad {

    private final SickNoteStatisticsService sickNoteStatisticsService;
    private final PersonService personService;
    private final Clock clock;

    @Autowired
    SickNoteStatisticsViewController(SickNoteStatisticsService sickNoteStatisticsService, PersonService personService, Clock clock) {
        this.sickNoteStatisticsService = sickNoteStatisticsService;
        this.personService = personService;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @GetMapping
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Integer requestedYear, Model model) {

        final Person signedInUser = personService.getSignedInUser();
        final Clock clockOfRequestedYear = getClockOfRequestedYear(requestedYear);
        final SickNoteStatistics statistics = sickNoteStatisticsService.createStatisticsForPerson(signedInUser, clockOfRequestedYear);

        model.addAttribute("statistics", statistics);
        model.addAttribute("currentYear", Year.now(clock).getValue());

        final GraphDto graphDto = new GraphDto(
            List.of("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"),
            "Tage",
            List.of(
                new DataSeries("Krank", statistics.getNumberOfSickDaysByMonth()),
                new DataSeries("Kind-Krank", statistics.getNumberOfChildSickDaysByMonth())
            )
        );
        model.addAttribute("sickNoteGraphStatistic", graphDto);

        return "sicknote/sick_notes_statistics";
    }

    private Clock getClockOfRequestedYear(Integer requestedYear) {
        if (requestedYear == null) {
            requestedYear = Year.now(clock).getValue();
        }
        return Clock.fixed(ZonedDateTime.now(clock).withYear(requestedYear).toInstant(), clock.getZone());
    }

    record GraphDto(List<String> xaxisLabels, String yaxisTitle, List<DataSeries> dataSeries) {};

    record DataSeries(String name, List<BigDecimal> data) {}
}
