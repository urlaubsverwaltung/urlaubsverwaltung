package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.nCopies;
import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsViewControllerTest {

    private OvertimeStatisticsViewController sut;

    @Mock
    private OvertimeStatisticsService statisticsService;
    @Mock
    private SettingsService settingsService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-31T10:15:00Z"), UTC);

    @BeforeEach
    void setUp() {
        final StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("hours.abbr", GERMAN, "Std.");
        messageSource.addMessage("minutes.abbr", GERMAN, "Min.");
        messageSource.addMessage("overtime.person.zero", GERMAN, "keine");

        sut = new OvertimeStatisticsViewController(statisticsService, settingsService, messageSource, clock);

        // most tests are about the selected year, so an empty company wide history is the default
        lenient().when(statisticsService.getTotals()).thenReturn(OvertimeTotals.empty());
        // every request also loads the previous year for the comparison curve
        lenient().when(statisticsService.getStatistics(any()))
            .thenAnswer(invocation -> OvertimeStatistics.empty(invocation.getArgument(0)));
    }

    @Test
    void ensureStatisticsForCurrentYearWhenNoYearIsRequested() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(ZERO), months(ZERO));

        perform(get("/web/overtime/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2026))
            .andExpect(model().attribute("currentYear", 2026))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void ensureStatisticsForRequestedYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2024), months(ZERO), months(ZERO));

        perform(get("/web/overtime/statistics").param("year", "2024"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2024))
            .andExpect(model().attribute("currentYear", 2026))
            .andExpect(view().name("overtime/overtime_statistics"));
    }

    @Test
    void ensureUnparsableYearFallsBackToCurrentYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(ZERO), months(ZERO));

        perform(get("/web/overtime/statistics").param("year", "not-a-year"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("selectedYear", 2026));
    }

    @Test
    void ensureNotFoundWhenOvertimeFeatureIsDeactivated() throws Exception {

        overtimeFeature(false);

        perform(get("/web/overtime/statistics"))
            .andExpect(status().isNotFound());
    }

    @Test
    void ensureGraphHasAccrualUpwardsAndReductionDownwardsInDecimalHours() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.MonthlySeriesDto graph = selectedSeriesOf(result);

        assertThat(graph.accrued()).hasSize(12);
        assertThat(graph.reduction()).hasSize(12);
        assertThat(graph.accrued().get(0)).isEqualByComparingTo(new BigDecimal("2.50"));
        // reduction is handed over negated, so the bars point downwards from the zero line
        assertThat(graph.reduction().get(0)).isEqualByComparingTo(new BigDecimal("-2.00"));
    }

    @Test
    void ensureGraphCarriesFormattedValuesForTooltipAndTable() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.MonthlySeriesDto graph = selectedSeriesOf(result);

        assertThat(graph.accruedText().get(0)).isEqualTo("2 Std. 30 Min.");
        assertThat(graph.reductionText().get(0)).isEqualTo("2 Std.");
        assertThat(graph.balanceText().get(0)).isEqualTo("30 Min.");
    }

    @Test
    void ensureGraphCarriesThePreviousYearBesideTheSelectedOne() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));
        statisticsOf(Year.of(2025), months(Duration.ofHours(1)), months(Duration.ofMinutes(30)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        // the selected year first, the previous year second
        assertThat(graphOf(result).series())
            .extracting(OvertimeStatisticsViewController.MonthlySeriesDto::year)
            .containsExactly(2026, 2025);
        assertThat(graphOf(result).series().get(1).accrued().get(0)).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void ensureGraphLeavesOutAPreviousYearWithoutAnyOvertime() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));
        statisticsOf(Year.of(2025), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        // a row of empty bars next to every month would say nothing
        assertThat(graphOf(result).series())
            .extracting(OvertimeStatisticsViewController.MonthlySeriesDto::year)
            .containsExactly(2026);
    }

    @Test
    void ensureNegativeMonthlyBalanceIsFormattedWithSign() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(1)), months(Duration.ofHours(3)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(selectedSeriesOf(result).balanceText().get(0)).isEqualTo("-2 Std.");
    }

    @Test
    void ensureStatisticsAreRequestedForTheSelectedYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2024), months(ZERO), months(ZERO));

        perform(get("/web/overtime/statistics").param("year", "2024"))
            .andExpect(status().isOk());
    }

    @Test
    void ensureYearSummaryCarriesTheTotalsOfTheSelectedYear() throws Exception {

        overtimeFeature(true);
        // twelve months of two and a half hours accrued and two hours reduced
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.YearSummaryDto summary = yearSummaryOf(result);

        assertThat(summary.accrued()).isEqualTo("30 Std.");
        assertThat(summary.reduction()).isEqualTo("24 Std.");
        assertThat(summary.balance()).isEqualTo("6 Std.");
    }

    @Test
    void ensureNegativeYearBalanceIsFormattedWithSign() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(1)), months(Duration.ofHours(3)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(yearSummaryOf(result).balance()).isEqualTo("-24 Std.");
    }

    @Test
    void ensureYearWithoutAnyOvertimeShowsTheZeroTextInsteadOfNothing() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.YearSummaryDto summary = yearSummaryOf(result);

        assertThat(summary.accrued()).isEqualTo("keine");
        assertThat(summary.reduction()).isEqualTo("keine");
        assertThat(summary.balance()).isEqualTo("keine");
    }

    @Test
    void ensureYearSummaryIsTheSumOfTheChartValues() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final BigDecimal accruedFromChart = selectedSeriesOf(result).accrued().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // the tiles must not tell a different story than the bars above them
        assertThat(accruedFromChart).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(yearSummaryOf(result).accrued()).isEqualTo("30 Std.");
    }

    @Test
    void ensureTotalsCarryTheFiguresOfTheWholeHistory() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(ZERO), months(ZERO));
        when(statisticsService.getTotals()).thenReturn(new OvertimeTotals(Duration.ofHours(427), Duration.ofMinutes(21030)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.TotalsDto totals = totalsOf(result);

        assertThat(totals.accrued()).isEqualTo("427 Std.");
        assertThat(totals.reduction()).isEqualTo("350 Std. 30 Min.");
        assertThat(totals.balance()).isEqualTo("76 Std. 30 Min.");
    }

    @Test
    void ensureTotalsDoNotDependOnTheSelectedYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2024), months(ZERO), months(ZERO));
        when(statisticsService.getTotals()).thenReturn(new OvertimeTotals(Duration.ofHours(427), Duration.ofHours(350)));

        final MvcResult result = perform(get("/web/overtime/statistics").param("year", "2024"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(totalsOf(result).accrued()).isEqualTo("427 Std.");
        // the totals are fetched without handing over any year
        verify(statisticsService).getTotals();
    }

    @Test
    void ensureEmptyCompanyShowsTheZeroTextInTheTotals() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(ZERO), months(ZERO));
        when(statisticsService.getTotals()).thenReturn(new OvertimeTotals(ZERO, ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(totalsOf(result).balance()).isEqualTo("keine");
    }

    @Test
    void ensureBalanceGraphCumulatesTheSelectedYearStartingAtZero() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(5)), months(Duration.ofHours(2)));
        statisticsOf(Year.of(2025), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final OvertimeStatisticsViewController.BalanceSeriesDto selectedYear = balanceGraphOf(result).series().get(0);

        assertThat(selectedYear.year()).isEqualTo(2026);
        assertThat(selectedYear.values()).hasSize(12);
        assertThat(selectedYear.values().get(0)).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(selectedYear.values().get(1)).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(selectedYear.values().get(11)).isEqualByComparingTo(new BigDecimal("36.00"));
    }

    @Test
    void ensureBalanceGraphHasASecondSeriesForThePreviousYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(5)), months(ZERO));
        statisticsOf(Year.of(2025), months(Duration.ofHours(1)), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        final List<OvertimeStatisticsViewController.BalanceSeriesDto> series = balanceGraphOf(result).series();

        assertThat(series).hasSize(2);
        assertThat(series.get(0).year()).isEqualTo(2026);
        assertThat(series.get(1).year()).isEqualTo(2025);
        assertThat(series.get(1).values().get(11)).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    void ensurePreviousYearWithoutAnyOvertimeIsLeftOutInsteadOfDrawingAFlatLine() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(5)), months(ZERO));
        statisticsOf(Year.of(2025), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(balanceGraphOf(result).series()).hasSize(1);
    }

    @Test
    void ensureBalanceGraphCarriesFormattedValuesForTheTooltip() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));
        statisticsOf(Year.of(2025), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(balanceGraphOf(result).series().get(0).valuesText().get(0)).isEqualTo("30 Min.");
        assertThat(balanceGraphOf(result).series().get(0).valuesText().get(1)).isEqualTo("1 Std.");
    }

    @Test
    void ensureTheLastPointOfTheCurveIsTheBalanceCardOfTheYear() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofMinutes(150)), months(Duration.ofHours(2)));
        statisticsOf(Year.of(2025), months(ZERO), months(ZERO));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        // the curve must end where the card says the year ended, otherwise the page contradicts itself
        assertThat(balanceGraphOf(result).series().get(0).valuesText().get(11))
            .isEqualTo(yearSummaryOf(result).balance());
    }

    private static OvertimeStatisticsViewController.BalanceGraphDto balanceGraphOf(MvcResult result) {
        return (OvertimeStatisticsViewController.BalanceGraphDto) result.getModelAndView().getModel().get("overtimeBalanceGraph");
    }

    private static OvertimeStatisticsViewController.TotalsDto totalsOf(MvcResult result) {
        return (OvertimeStatisticsViewController.TotalsDto) result.getModelAndView().getModel().get("overtimeTotals");
    }

    private static OvertimeStatisticsViewController.YearSummaryDto yearSummaryOf(MvcResult result) {
        return (OvertimeStatisticsViewController.YearSummaryDto) result.getModelAndView().getModel().get("overtimeYearSummary");
    }

    private void statisticsOf(Year year, List<Duration> accrued, List<Duration> reduction) {
        when(statisticsService.getStatistics(year)).thenReturn(new OvertimeStatistics(year, accrued, reduction));
    }

    private static List<Duration> months(Duration duration) {
        return nCopies(12, duration);
    }

    private static OvertimeStatisticsViewController.GraphDto graphOf(MvcResult result) {
        return (OvertimeStatisticsViewController.GraphDto) result.getModelAndView().getModel().get("overtimeGraph");
    }

    private static OvertimeStatisticsViewController.MonthlySeriesDto selectedSeriesOf(MvcResult result) {
        return graphOf(result).series().get(0);
    }

    private void overtimeFeature(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder.locale(GERMAN));
    }
}
