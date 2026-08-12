package org.synyx.urlaubsverwaltung.sicknote.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

/**
 * Controller for statistics of sick notes resp. sick days.
 */
@Controller
@RequestMapping("/web/sicknote/statistics")
class SickNoteStatisticsViewController implements HasLaunchpad, HasPersonSearch {

    private final SickNoteStatisticsService sickNoteStatisticsService;
    private final PersonService personService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    SickNoteStatisticsViewController(
        SickNoteStatisticsService sickNoteStatisticsService,
        PersonService personService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.sickNoteStatisticsService = sickNoteStatisticsService;
        this.personService = personService;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    @PreAuthorize("hasAnyAuthority('OFFICE', 'SICK_NOTE_VIEW', 'DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY')")
    @GetMapping
    public String sickNotesStatistics(@RequestParam(value = "year", required = false) Optional<Year> userRequestedYear, Model model) {

        final Year selectedYear = userRequestedYear.orElse(Year.now(clock));
        final Person signedInUser = personService.getSignedInUser();

        final LocalDate selectedYearFirstDay = selectedYear.atDay(1);
        final LocalDate selectedYearLastDay = selectedYearFirstDay.with(lastDayOfYear());
        final SickNoteStatistics selectedYearStatistics = sickNoteStatisticsService.createStatisticsForPerson(selectedYearFirstDay, selectedYearLastDay, signedInUser);
        model.addAttribute("selectedYearStatistics", toSickNoteStatisticsDto(selectedYearStatistics));

        final Year previousSelectedYear = selectedYear.minusYears(1);
        final LocalDate previousSelectedYearFirstDay = previousSelectedYear.atDay(1);
        final LocalDate previousSelectedYearLastDay = previousSelectedYearFirstDay.with(lastDayOfYear());
        final SickNoteStatistics previousSelectedYearStatistics = sickNoteStatisticsService.createStatisticsForPerson(previousSelectedYearFirstDay, previousSelectedYearLastDay, signedInUser);
        model.addAttribute("previousSelectedYearStatistics", toSickNoteStatisticsDto(previousSelectedYearStatistics));

        final GraphDto graphDto = new GraphDto(
            List.of(
                new DataSeries(selectedYear.getValue(), selectedYearStatistics.getNumberOfSickDaysByMonth()),
                new DataSeries(selectedYear.getValue(), selectedYearStatistics.getNumberOfChildSickDaysByMonth()),
                new DataSeries(previousSelectedYearStatistics.getYear(), previousSelectedYearStatistics.getNumberOfSickDaysByMonth()),
                new DataSeries(previousSelectedYearStatistics.getYear(), previousSelectedYearStatistics.getNumberOfChildSickDaysByMonth())
            ),
            List.of(
                new DataSeries(selectedYear.getValue(), selectedYearStatistics.getSickRateByMonth()),
                new DataSeries(previousSelectedYearStatistics.getYear(), previousSelectedYearStatistics.getSickRateByMonth())
            ),
            List.of(
                selectedYearStatistics.getAtLeastOneSickNotePercent(),
                previousSelectedYearStatistics.getAtLeastOneSickNotePercent()
            )
        );
        model.addAttribute("sickNoteGraphStatistic", graphDto);

        model.addAttribute("currentYear", Year.now(clock).getValue());

        return "sicknote/sick_notes_statistics";
    }

    private SickNoteStatisticsDto toSickNoteStatisticsDto(SickNoteStatistics sickNoteStatistics) {
        return new SickNoteStatisticsDto(
            sickNoteStatistics.getYear(),
            sickNoteStatistics.getAsOfDate(),
            sickNoteStatistics.getNumberOfSickDaysByMonth(),
            sickNoteStatistics.getNumberOfChildSickDaysByMonth(),
            sickNoteStatistics.getSickRateByMonth(),
            sickNoteStatistics.getSickRate(),
            sickNoteStatistics.getTotalNumberOfAllSickNotes(),
            sickNoteStatistics.getTotalNumberOfSickNotes(),
            sickNoteStatistics.getTotalNumberOfChildSickNotes(),
            sickNoteStatistics.getAtLeastOneSickNotePercent(),
            sickNoteStatistics.getNumberOfPersonsWithMinimumOneSickNote(),
            sickNoteStatistics.getNumberOfPersonsWithoutSickNote(),
            sickNoteStatistics.getTotalNumberOfSickDaysAllCategories(),
            sickNoteStatistics.getTotalNumberOfSickDays(),
            sickNoteStatistics.getTotalNumberOfChildSickDays(),
            sickNoteStatistics.getAverageDurationOfAllSickNotes(),
            sickNoteStatistics.getAverageDurationOfSickNote(),
            sickNoteStatistics.getAverageDurationOfChildSickNote(),
            sickNoteStatistics.getAverageDurationOfDiseasePerPerson(),
            sickNoteStatistics.getAverageDurationOfDiseasePerPersonAndSick(),
            sickNoteStatistics.getAverageDurationOfDiseasePerPersonAndChildSick()
        );
    }

    // changing this GraphDto, you may have to increase the local-storage version key in JavaScript to keep the local state clean!
    record GraphDto(List<DataSeries> dataSeries, List<DataSeries> sickRateDataSeries, List<BigDecimal> dataSeriesRadial) {
    }

    record DataSeries(int year, List<BigDecimal> data) {
    }
}
