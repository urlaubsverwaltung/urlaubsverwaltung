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
import java.util.Optional;

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
    SickNoteStatisticsViewController(
        SickNoteStatisticsService sickNoteStatisticsService,
        PersonService personService,
        Clock clock
    ) {
        this.sickNoteStatisticsService = sickNoteStatisticsService;
        this.personService = personService;
        this.clock = clock;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW')")
    @GetMapping
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Optional<Year> userRequestedYear, Model model) {

        final Year selectedYear = userRequestedYear.orElse(Year.now(clock));
        final Person signedInUser = personService.getSignedInUser();

        final SickNoteStatistics selectedYearStatistics = sickNoteStatisticsService.createStatisticsForPerson(signedInUser, getClockOfRequestedYear(selectedYear));
        model.addAttribute("selectedYearStatistics", selectedYearStatistics);

        final SickNoteStatistics previousSelectedYearStatistics = sickNoteStatisticsService.createStatisticsForPerson(signedInUser, getClockOfRequestedYear(selectedYear.minusYears(1)));
        model.addAttribute("previousSelectedYearStatistics", previousSelectedYearStatistics);

        final GraphDto graphDto = new GraphDto(
            List.of(
                new DataSeries(selectedYearStatistics.getNumberOfSickDaysByMonth()),
                new DataSeries(selectedYearStatistics.getNumberOfChildSickDaysByMonth()),
                new DataSeries(previousSelectedYearStatistics.getNumberOfSickDaysByMonth()),
                new DataSeries(previousSelectedYearStatistics.getNumberOfChildSickDaysByMonth())
            )
        );
        model.addAttribute("sickNoteGraphStatistic", graphDto);

        model.addAttribute("currentYear", Year.now(clock).getValue());

        return "sicknote/sick_notes_statistics";
    }

    private Clock getClockOfRequestedYear(final Year year) {
        return Clock.fixed(ZonedDateTime.now(clock).withYear(year.getValue()).toInstant(), clock.getZone());
    }

    record GraphDto(List<DataSeries> dataSeries) {
    }

    record DataSeries(List<BigDecimal> data) {
    }
}
