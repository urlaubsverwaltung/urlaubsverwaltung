package org.synyx.urlaubsverwaltung.company;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistribution;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistributionEntry;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Controller
@RequestMapping("/web/company")
@PreAuthorize(IS_BOSS_OR_OFFICE)
class CompanyController implements HasLaunchpad, HasPersonSearch {

    private final PersonService personService;
    private final OvertimeStatisticService overtimeStatisticService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    CompanyController(
        PersonService personService,
        OvertimeStatisticService overtimeStatisticService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.personService = personService;
        this.overtimeStatisticService = overtimeStatisticService;
        this.defaultPersonSuggestionUrlStrategy = defaultPersonSuggestionUrlStrategy;
        this.personSearchUiFragmentSupplier = personSearchUiFragmentSupplier;
        this.clock = clock;
    }

    @GetMapping
    public String company() {
        return "redirect:/web/company/overview";
    }

    @GetMapping("/overview")
    public String overview(Model model, @RequestParam("year") Optional<String> yearMode) {

        final Person signedInUser = personService.getSignedInUser();

        final DateRange dateRange;
        if (yearMode.isPresent()) {
            final Year year = Year.now(clock);
            dateRange = new DateRange(year.atDay(1), year.atMonth(DECEMBER).atEndOfMonth());
        } else {
            final YearMonth month = YearMonth.now(clock);
            dateRange = new DateRange(month.atDay(1), month.atEndOfMonth());
        }

        final String mode;
        if (yearMode.isPresent()) {
            mode = "year";
        } else {
            mode = "month";
        }

        final OvertimeStatistic stats = overtimeStatisticService.getOvertimeStatistics(signedInUser, dateRange.start, dateRange.end);

        final DateRange previousDateRange = toPreviousRange(dateRange);
        final OvertimeStatistic prevStats = overtimeStatisticService.getOvertimeStatistics(signedInUser, previousDateRange.start, previousDateRange.end);

        final BigDecimal average = toBigDecimalHours(stats.average());
        final BigDecimal averagePrev = toBigDecimalHours(prevStats.average());

        final CompanyStatisticsDto statisticsDto = new CompanyStatisticsDto(
            dateRange.start, dateRange.end, average, average.subtract(averagePrev),
            new OvertimeDistribution(stats.personCount(), List.of(
                new OvertimeDistributionEntry(0, 5, stats.numberOfPersonsWithDurationBetween(hours(0), hours(5))),
                new OvertimeDistributionEntry(5, 15, stats.numberOfPersonsWithDurationBetween(hours(5), hours(15))),
                new OvertimeDistributionEntry(15, 25, stats.numberOfPersonsWithDurationBetween(hours(15), hours(25))),
                new OvertimeDistributionEntry(25, null, stats.numberOfPersonsWithDurationGreaterOrEqual(hours(25))
            ))
        ));

        model.addAttribute("mode", mode);
        model.addAttribute("statistics", statisticsDto);

        return "company/company-overview";
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    record DateRange(LocalDate start, LocalDate end) {}

    private static DateRange toPreviousRange(DateRange dateRange) {
        final long days = DAYS.between(dateRange.start, dateRange.end) + 1;
        final LocalDate previousEnd = dateRange.start.minusDays(1);
        final LocalDate previousStart = previousEnd.minusDays(days - 1);
        return new DateRange(previousStart, previousEnd);
    }

    private static Duration hours(int value) {
        return Duration.ofHours(value);
    }

    private static BigDecimal toBigDecimalHours(Duration duration) {
        return BigDecimal.valueOf((double) duration.toMinutes() / 60);
    }
}
