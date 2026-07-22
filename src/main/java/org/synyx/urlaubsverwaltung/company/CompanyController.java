package org.synyx.urlaubsverwaltung.company;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistributionDto;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistributionEntryDto;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDurationDto;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

@Controller
@RequestMapping("/web/company")
@PreAuthorize(IS_BOSS_OR_OFFICE)
class CompanyController implements HasLaunchpad, HasPersonSearch {

    private static final ZoneId USER_ZONE = ZoneId.of("Europe/Berlin");

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
    public String overview(Model model, @RequestParam("view") Optional<String> view) {

        final Person signedInUser = personService.getSignedInUser();

        final ViewMode viewMode = view.flatMap(ViewMode::of).orElse(ViewMode.MONTH);
        final DateRange dateRange = getRequestedDateRange(viewMode);

        final OvertimeStatistic stats = overtimeStatisticService.getOvertimeStatistics(signedInUser, dateRange.start, dateRange.end);

        final DateRange previousDateRange = toPreviousRange(dateRange);
        final OvertimeStatistic prevStats = overtimeStatisticService.getOvertimeStatistics(signedInUser, previousDateRange.start, previousDateRange.end);

        final Duration average = stats.average();
        final Duration averagePrev = prevStats.average();
        final Duration averageGrowth = average.minus(averagePrev);

        final CompanyStatisticsDto statisticsDto = new CompanyStatisticsDto(
            toLocalDate(dateRange.start), toLocalDate(dateRange.end),
            toOvertimeDurationDto(average), toOvertimeDurationDto(averageGrowth),
            new OvertimeDistributionDto(stats.personCount(), List.of(
                new OvertimeDistributionEntryDto(0, 5, stats.numberOfPersonsWithDurationBetween(hours(0), hours(5))),
                new OvertimeDistributionEntryDto(5, 15, stats.numberOfPersonsWithDurationBetween(hours(5), hours(15))),
                new OvertimeDistributionEntryDto(15, 25, stats.numberOfPersonsWithDurationBetween(hours(15), hours(25))),
                new OvertimeDistributionEntryDto(25, null, stats.numberOfPersonsWithDurationGreaterOrEqual(hours(25))
            ))
        ));

        model.addAttribute("viewMode", viewMode.name().toLowerCase());
        model.addAttribute("statistics", statisticsDto);

        return "company/company-overview";
    }

    private DateRange getRequestedDateRange(ViewMode viewMode) {
        final Clock userClock = clock.withZone(USER_ZONE);
        final YearMonth month = YearMonth.now(userClock);
        final Instant now = toInstant(LocalDate.now(userClock));
        return switch (viewMode) {
            case YEAR -> new DateRange(toInstant(Year.of(month.getYear()).atDay(1)), now);
            case QUARTER -> new DateRange(toInstant(month.minusMonths(2).atDay(1)), now);
            case MONTH -> new DateRange(toInstant(month.atDay(1)), now);
        };
    }

    @Override
    public PersonSuggestionUrlStrategy personSuggestionUrlStrategy() {
        return defaultPersonSuggestionUrlStrategy;
    }

    @Override
    public PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier() {
        return personSearchUiFragmentSupplier;
    }

    enum ViewMode {
        MONTH,
        QUARTER,
        YEAR;

        static Optional<ViewMode> of(String value) {
            if (value == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(ViewMode.valueOf(value.toUpperCase()));
            } catch(IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    record DateRange(Instant start, Instant end) {}

    private static DateRange toPreviousRange(DateRange dateRange) {
        final long days = DAYS.between(dateRange.start, dateRange.end) + 1;
        final Instant previousEnd = dateRange.start.minus(1, DAYS);
        final Instant previousStart = previousEnd.minus(days - 1, DAYS);
        return new DateRange(previousStart, previousEnd);
    }

    private static Instant toInstant(LocalDate date) {
        return date.atStartOfDay().toInstant(UTC);
    }

    private static LocalDate toLocalDate(Instant instant) {
        return LocalDate.ofInstant(instant, UTC);
    }

    private static Duration hours(int value) {
        return Duration.ofHours(value);
    }

    private static OvertimeDurationDto toOvertimeDurationDto(Duration duration) {
        final boolean negative = duration.isNegative();
        final Duration abs = duration.abs();
        return new OvertimeDurationDto(negative, (int) abs.toHours(), abs.toMinutesPart());
    }
}
