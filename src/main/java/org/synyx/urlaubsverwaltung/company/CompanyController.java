package org.synyx.urlaubsverwaltung.company;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.company.OvertimeStatDto.OvertimeDistributionDto;
import org.synyx.urlaubsverwaltung.company.OvertimeStatDto.OvertimeDistributionEntryDto;
import org.synyx.urlaubsverwaltung.company.OvertimeStatDto.OvertimeDurationDto;
import org.synyx.urlaubsverwaltung.company.VacationDaysStatDto.RemainingVacationDaysDistributionDto;
import org.synyx.urlaubsverwaltung.company.VacationDaysStatDto.RemainingVacationDaysDistributionEntryDto;
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
    private final HealthOvertimeStatisticService overtimeStatisticService;
    private final HealthSickDaysStatisticService sickDaysStatisticService;
    private final HealthVacationDaysStatisticService vacationDaysStatisticService;
    private final PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    private final PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;
    private final Clock clock;

    CompanyController(
        PersonService personService,
        HealthOvertimeStatisticService overtimeStatisticService,
        HealthSickDaysStatisticService sickDaysStatisticService,
        HealthVacationDaysStatisticService vacationDaysStatisticService,
        PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy,
        PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier,
        Clock clock
    ) {
        this.personService = personService;
        this.overtimeStatisticService = overtimeStatisticService;
        this.sickDaysStatisticService = sickDaysStatisticService;
        this.vacationDaysStatisticService = vacationDaysStatisticService;
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
        final DateRange previousDateRange = toPreviousRange(dateRange);

        final OvertimeStatDto overtimeStatDto = overtimeStatistic(signedInUser, dateRange, previousDateRange);
        final SickDaysStatDto sickDaysStatDto = sickDaysStatistic(signedInUser, dateRange);
        final VacationDaysStatDto vacationDaysStatDto = vacationDaysStatistic(signedInUser, dateRange);

        model.addAttribute("viewMode", viewMode.name().toLowerCase());
        model.addAttribute("dateRangeStart", toLocalDate(dateRange.start));
        model.addAttribute("dateRangeEnd", toLocalDate(dateRange.end));
        model.addAttribute("overtimeStatistic", overtimeStatDto);
        model.addAttribute("sickDaysStatistic", sickDaysStatDto);
        model.addAttribute("vacationDaysStatistic", vacationDaysStatDto);

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

    private OvertimeStatDto overtimeStatistic(Person signedInUser, DateRange dateRange, DateRange previousDateRange) {
        final OvertimeStatistic stats = overtimeStatisticService.getOvertimeStatistics(signedInUser, dateRange.start, dateRange.end);
        final OvertimeStatistic prevStats = overtimeStatisticService.getOvertimeStatistics(signedInUser, previousDateRange.start, previousDateRange.end);

        final Duration average = stats.average();
        final Duration averagePrev = prevStats.average();
        final Duration averageGrowth = average.minus(averagePrev);

        final List<Integer[]> ranges = List.of(
            new Integer[]{ 0, 5 },
            new Integer[]{ 5, 15 },
            new Integer[]{ 15, 25 },
            new Integer[]{ 25, null }
        );

        final List<OvertimeDistributionEntryDto> distributionDtos =
            ranges.stream().map(range -> toOvertimeDistributionEntryDto(range, stats)).toList();

        return new OvertimeStatDto(
            toOvertimeDurationDto(average),
            toOvertimeDurationDto(averageGrowth),
            new OvertimeDistributionDto(stats.personCount(), distributionDtos)
        );
    }

    private static OvertimeDistributionEntryDto toOvertimeDistributionEntryDto(Integer[] range, OvertimeStatistic stats) {
        final Integer rangeStart = range[0];
        final Integer rangeEnd = range[1];

        final int value;
        if (rangeEnd == null) {
            value = stats.numberOfPersonsWithDurationGreaterOrEqual(hours(rangeStart));
        } else {
            value = stats.numberOfPersonsWithDurationBetween(hours(rangeStart), hours(rangeEnd));
        }

        return new OvertimeDistributionEntryDto(range[0], range[1], value);
    }

    private SickDaysStatDto sickDaysStatistic(Person signedInUser, DateRange dateRange) {

        final LocalDate from = toLocalDate(dateRange.start);
        final LocalDate to = toLocalDate(dateRange.end);

        final SickDaysStatistic stats = sickDaysStatisticService.getSickDaysStatistics(signedInUser, from, to);

        final double healthRate = stats.healthRate().value();
        final int nrOfSickDays = stats.totalNumberOfAllSickNotes().intValue();
        final int nrOfShouldWorkDays = stats.shouldWorkDays().intValue();

        return new SickDaysStatDto(healthRate, nrOfSickDays, nrOfShouldWorkDays, stats.distribution());
    }

    private VacationDaysStatDto vacationDaysStatistic(Person signedInUser, DateRange dateRange) {

        final LocalDate from = toLocalDate(dateRange.start);
        final LocalDate to = toLocalDate(dateRange.end);

        final VacationDaysStatistic stats = vacationDaysStatisticService.getVacationDaysStatistic(signedInUser, from, to);

        final List<Double[]> ranges = List.of(
            new Double[]{  0.0, 10.0 },
            new Double[]{ 10.0, 18.0 },
            new Double[]{ 18.0, 25.0 },
            new Double[]{ 25.0, null }
        );

        final List<RemainingVacationDaysDistributionEntryDto> distributionDtos =
            ranges.stream().map(range -> toDistributionEntryDto(range, stats)).toList();

        return new VacationDaysStatDto(
            new RemainingVacationDaysDistributionDto(stats.personCount(), distributionDtos)
        );
    }

    private static RemainingVacationDaysDistributionEntryDto toDistributionEntryDto(Double[] range, VacationDaysStatistic stats) {

        final Double rangeStart = range[0];
        final Double rangeEnd = range[1];

        final int value;

        if (rangeEnd == null) {
            value = stats.numberOfPersonsWithRemainingVacationDaysGreaterThan(rangeStart);
        } else {
            value = stats.numberOfPersonsWithRemainingVacationDaysBetween(rangeStart, rangeEnd);
        }

        return new RemainingVacationDaysDistributionEntryDto(rangeStart, rangeEnd, value);
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
