package org.synyx.urlaubsverwaltung.overtime.statistics;

import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.math.RoundingMode.HALF_UP;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.synyx.urlaubsverwaltung.security.SecurityRules.IS_BOSS_OR_OFFICE;

/**
 * Controller for the company wide overtime statistics.
 */
@Controller
@RequestMapping("/web/overtime/statistics")
@PreAuthorize(IS_BOSS_OR_OFFICE)
class OvertimeStatisticsViewController implements HasLaunchpad {

    private static final int MINUTES_PER_HOUR = 60;
    private static final int DECIMAL_HOUR_SCALE = 2;

    private final OvertimeStatisticsService overtimeStatisticsService;
    private final SettingsService settingsService;
    private final MessageSource messageSource;
    private final Clock clock;

    OvertimeStatisticsViewController(
        OvertimeStatisticsService overtimeStatisticsService,
        SettingsService settingsService,
        MessageSource messageSource,
        Clock clock
    ) {
        this.overtimeStatisticsService = overtimeStatisticsService;
        this.settingsService = settingsService;
        this.messageSource = messageSource;
        this.clock = clock;
    }

    @GetMapping
    public String overtimeStatistics(
        @RequestParam(value = "year", required = false) Optional<String> requestedYear,
        Locale locale,
        Model model
    ) {

        if (!settingsService.getSettings().getOvertimeSettings().isOvertimeActive()) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        final Year currentYear = Year.now(clock);
        final Year selectedYear = requestedYear.flatMap(OvertimeStatisticsViewController::toYear).orElse(currentYear);

        final OvertimeStatistics statistics = overtimeStatisticsService.getStatistics(selectedYear);

        model.addAttribute("selectedYear", selectedYear.getValue());
        model.addAttribute("currentYear", currentYear.getValue());
        model.addAttribute("overtimeGraph", toGraphDto(statistics, locale));
        model.addAttribute("overtimeYearSummary", toYearSummaryDto(statistics, locale));

        final OvertimeStatistics previousStatistics = overtimeStatisticsService.getStatistics(selectedYear.minusYears(1));
        model.addAttribute("overtimeBalanceGraph", toBalanceGraphDto(statistics, previousStatistics, locale));

        // deliberately without the selected year, these figures cover the whole history
        final OvertimeTotals totals = overtimeStatisticsService.getTotals();
        model.addAttribute("overtimeTotals", toTotalsDto(totals, locale));

        return "overtime/overtime_statistics";
    }

    private BalanceGraphDto toBalanceGraphDto(OvertimeStatistics statistics, OvertimeStatistics previous, Locale locale) {

        final List<BalanceSeriesDto> series = new ArrayList<>();
        series.add(toBalanceSeriesDto(statistics, locale));

        // a year in which nothing happened at all would only add a flat line at zero, which says nothing
        if (!previous.hasNoOvertime()) {
            series.add(toBalanceSeriesDto(previous, locale));
        }

        return new BalanceGraphDto(List.copyOf(series));
    }

    private BalanceSeriesDto toBalanceSeriesDto(OvertimeStatistics statistics, Locale locale) {
        final List<Duration> cumulative = statistics.cumulativeBalanceByMonth();
        return new BalanceSeriesDto(
            statistics.year().getValue(),
            cumulative.stream().map(OvertimeStatisticsViewController::toDecimalHours).toList(),
            toTexts(cumulative, locale)
        );
    }

    private TotalsDto toTotalsDto(OvertimeTotals totals, Locale locale) {
        return new TotalsDto(
            toText(totals.accrued(), locale),
            toText(totals.reduction(), locale),
            toText(totals.balance(), locale)
        );
    }

    private YearSummaryDto toYearSummaryDto(OvertimeStatistics statistics, Locale locale) {
        return new YearSummaryDto(
            toText(statistics.accrued(), locale),
            toText(statistics.reduction(), locale),
            toText(statistics.balance(), locale)
        );
    }

    private GraphDto toGraphDto(OvertimeStatistics statistics, Locale locale) {
        return new GraphDto(
            statistics.accruedByMonth().stream().map(OvertimeStatisticsViewController::toDecimalHours).toList(),
            // negated, so that apexcharts renders the reduction below the zero line
            statistics.reductionByMonth().stream().map(Duration::negated).map(OvertimeStatisticsViewController::toDecimalHours).toList(),
            toTexts(statistics.accruedByMonth(), locale),
            toTexts(statistics.reductionByMonth(), locale),
            toTexts(statistics.balanceByMonth(), locale)
        );
    }

    private List<String> toTexts(List<Duration> durations, Locale locale) {
        return durations.stream().map(duration -> toText(duration, locale)).toList();
    }

    private String toText(Duration duration, Locale locale) {
        return DurationFormatter.toDurationString(duration, messageSource, locale);
    }

    /**
     * Apexcharts needs plain numbers, therefore the chart axis works with decimal hours while every value shown to a
     * user is formatted by the {@link DurationFormatter}.
     */
    private static BigDecimal toDecimalHours(Duration duration) {
        return BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(MINUTES_PER_HOUR), DECIMAL_HOUR_SCALE, HALF_UP);
    }

    /**
     * The year is part of a shareable url, therefore an unusable value must not end in an error page.
     */
    private static Optional<Year> toYear(String value) {
        try {
            return Optional.of(Year.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Data of the monthly overtime chart.
     *
     * <p>
     * When changing this record, the local-storage version key in the JavaScript may need to be increased to keep the
     * persisted chart state clean.
     *
     * @param accrued       accrued hours per month, january first, positive
     * @param reduction     reduced hours per month, january first, negative so the bars point downwards
     * @param accruedText   accrued overtime per month, formatted for humans
     * @param reductionText reduced overtime per month, formatted for humans, without sign
     * @param balanceText   balance per month, formatted for humans
     */
    record GraphDto(
        List<BigDecimal> accrued,
        List<BigDecimal> reduction,
        List<String> accruedText,
        List<String> reductionText,
        List<String> balanceText
    ) {
    }

    /**
     * Totals of the selected year, formatted for humans.
     *
     * <p>
     * These are the sums of the monthly chart values, so the balance starts at zero on january first and carries
     * nothing over from earlier years. The figures over the whole history live in their own block above the year
     * selector.
     *
     * @param accrued   accrued overtime of the selected year
     * @param reduction reduced overtime of the selected year, without sign
     * @param balance   accrued minus reduced, negative when more was reduced than accrued
     */
    record YearSummaryDto(String accrued, String reduction, String balance) {
    }

    /**
     * Figures over the whole history, formatted for humans.
     *
     * <p>
     * These are shown above the year selector and do not react to it. The balance is the overtime the company still
     * has open, which is the same figure every person sees as their own remaining overtime, summed up.
     *
     * @param accrued   accrued overtime over the whole history
     * @param reduction reduced overtime over the whole history, without sign
     * @param balance   accrued minus reduced
     */
    record TotalsDto(String accrued, String reduction, String balance) {
    }

    /**
     * Data of the balance curve. One entry per year, the selected year first, the previous year second when it has
     * anything to show.
     */
    record BalanceGraphDto(List<BalanceSeriesDto> series) {
    }

    /**
     * One curve of the balance chart.
     *
     * @param year       the year this curve belongs to, also its label
     * @param values     cumulated balance per month in decimal hours, january first
     * @param valuesText the same values formatted for humans
     */
    record BalanceSeriesDto(int year, List<BigDecimal> values, List<String> valuesText) {
    }
}
