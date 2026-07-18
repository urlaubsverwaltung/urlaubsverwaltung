package org.synyx.urlaubsverwaltung.overtime.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.DeltaTrend;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

/**
 * Controller for the company-wide statistics of overtime.
 */
@Controller
@RequestMapping("/web/overtime/statistics")
class OvertimeStatisticsViewController implements HasLaunchpad, HasPersonSearch {

    private final OvertimeStatisticsService overtimeStatisticsService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    OvertimeStatisticsViewController(
        OvertimeStatisticsService overtimeStatisticsService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.overtimeStatisticsService = overtimeStatisticsService;
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

    private static final List<String> MONTH_MESSAGE_KEYS = List.of(
        "month.january", "month.february", "month.march", "month.april",
        "month.may", "month.june", "month.july", "month.august",
        "month.september", "month.october", "month.november", "month.december"
    );

    @PreAuthorize(IS_BOSS_OR_OFFICE)
    @GetMapping
    public String overtimeStatistics(@RequestParam(value = "year", required = false) Integer requestedYearValue, Model model) {

        final Year year = requestedYearValue == null ? Year.now(clock) : Year.of(requestedYearValue);

        final OvertimeStatistics statistics = overtimeStatisticsService.createStatistics(year);

        model.addAttribute("statistics", statistics);
        model.addAttribute("currentYear", Year.now(clock).getValue());

        final List<MonthlyDeltaDto> monthlyDeltas = IntStream.range(0, MONTH_MESSAGE_KEYS.size())
            .mapToObj(i -> new MonthlyDeltaDto(
                MONTH_MESSAGE_KEYS.get(i),
                statistics.getDeltaToPreviousMonthHours().get(i),
                statistics.getDeltaTrend().get(i)
            ))
            .toList();
        model.addAttribute("monthlyDeltas", monthlyDeltas);

        return "overtime/overtime_statistics";
    }

    record MonthlyDeltaDto(String monthMessageKey, BigDecimal deltaHours, DeltaTrend trend) {}
}
