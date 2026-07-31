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

        final OvertimeStatisticsViewController.GraphDto graph = graphOf(result);

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

        final OvertimeStatisticsViewController.GraphDto graph = graphOf(result);

        assertThat(graph.accruedText().get(0)).isEqualTo("2 Std. 30 Min.");
        assertThat(graph.reductionText().get(0)).isEqualTo("2 Std.");
        assertThat(graph.balanceText().get(0)).isEqualTo("30 Min.");
    }

    @Test
    void ensureNegativeMonthlyBalanceIsFormattedWithSign() throws Exception {

        overtimeFeature(true);
        statisticsOf(Year.of(2026), months(Duration.ofHours(1)), months(Duration.ofHours(3)));

        final MvcResult result = perform(get("/web/overtime/statistics")).andExpect(status().isOk()).andReturn();

        assertThat(graphOf(result).balanceText().get(0)).isEqualTo("-2 Std.");
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

        final BigDecimal accruedFromChart = graphOf(result).accrued().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // the tiles must not tell a different story than the bars above them
        assertThat(accruedFromChart).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(yearSummaryOf(result).accrued()).isEqualTo("30 Std.");
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

    private void overtimeFeature(boolean active) {
        final Settings settings = new Settings();
        settings.getOvertimeSettings().setOvertimeActive(active);
        when(settingsService.getSettings()).thenReturn(settings);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder.locale(GERMAN));
    }
}
