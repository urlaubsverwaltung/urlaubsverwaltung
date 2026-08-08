package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistributionDto;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDistributionEntryDto;
import org.synyx.urlaubsverwaltung.company.CompanyStatisticsDto.OvertimeDurationDto;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.time.Month.JANUARY;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    private CompanyController sut;

    @Mock
    private PersonService personService;
    @Mock
    private OvertimeStatisticService overtimeStatisticService;
    @Mock
    private PersonSuggestionUrlStrategy defaultPersonSuggestionUrlStrategy;
    @Mock
    private PersonSearchUiFragmentSupplier personSearchUiFragmentSupplier;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-19T10:00:00Z"), UTC);
    private final LocalDate today = LocalDate.now(clock);
    private final YearMonth currentMonth = YearMonth.now(clock);

    @BeforeEach
    void setUp() {
        sut = new CompanyController(personService, overtimeStatisticService, defaultPersonSuggestionUrlStrategy,
            personSearchUiFragmentSupplier, clock);
    }

    @Test
    void ensureCompanyRedirectsToOverview() throws Exception {
        perform(get("/web/company"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/company/overview"));
    }

    @Test
    void ensurePersonSuggestionUrlStrategy() {
        assertThat(sut.personSuggestionUrlStrategy()).isSameAs(defaultPersonSuggestionUrlStrategy);
    }

    @Test
    void ensurePersonSearchUiFragmentSupplier() {
        assertThat(sut.personSearchUiFragmentSupplier()).isSameAs(personSearchUiFragmentSupplier);
    }

    @Test
    void ensureOverviewDefaultsToMonthViewModeWhenViewParamIsAbsent() throws Exception {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        stubCurrentAndPreviousRange(signedInUser, currentMonth.atDay(1), today, OvertimeStatistic.empty());

        perform(get("/web/company/overview"))
            .andExpect(status().isOk())
            .andExpect(view().name("company/company-overview"))
            .andExpect(model().attribute("viewMode", "month"));
    }

    @Test
    void ensureOverviewDefaultsToMonthViewModeWhenViewParamIsInvalid() throws Exception {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        stubCurrentAndPreviousRange(signedInUser, currentMonth.atDay(1), today, OvertimeStatistic.empty());

        perform(get("/web/company/overview").param("view", "not-a-view-mode"))
            .andExpect(status().isOk())
            .andExpect(view().name("company/company-overview"))
            .andExpect(model().attribute("viewMode", "month"));
    }

    @Test
    void ensureOverviewUsesQuarterDateRange() throws Exception {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate start = currentMonth.minusMonths(2).atDay(1);
        stubCurrentAndPreviousRange(signedInUser, start, today, OvertimeStatistic.empty());

        perform(get("/web/company/overview").param("view", "quarter"))
            .andExpect(status().isOk())
            .andExpect(view().name("company/company-overview"))
            .andExpect(model().attribute("viewMode", "quarter"))
            .andExpect(model().attribute("statistics", equalTo(emptyStatisticsDto(start, today))));
    }

    @Test
    void ensureOverviewUsesYearDateRange() throws Exception {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate start = Year.of(currentMonth.getYear()).atDay(1);
        stubCurrentAndPreviousRange(signedInUser, start, today, OvertimeStatistic.empty());

        perform(get("/web/company/overview").param("view", "year"))
            .andExpect(status().isOk())
            .andExpect(view().name("company/company-overview"))
            .andExpect(model().attribute("viewMode", "year"))
            .andExpect(model().attribute("statistics", equalTo(emptyStatisticsDto(start, today))));
    }

    @Test
    void ensureOverviewUsesBerlinDateNearUtcMidnightBoundary() throws Exception {

        // 2026-01-14T23:30:00Z is already 2026-01-15T00:30 in Europe/Berlin (CET, UTC+1)
        final Clock nearMidnightUtcClock = Clock.fixed(Instant.parse("2026-01-14T23:30:00Z"), UTC);
        final CompanyController sutAtBerlinMidnight = new CompanyController(personService, overtimeStatisticService,
            defaultPersonSuggestionUrlStrategy, personSearchUiFragmentSupplier, nearMidnightUtcClock);

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate berlinToday = LocalDate.of(2026, JANUARY, 15);
        final LocalDate monthStart = LocalDate.of(2026, JANUARY, 1);
        stubCurrentAndPreviousRange(signedInUser, monthStart, berlinToday, OvertimeStatistic.empty());

        perform(get("/web/company/overview"), sutAtBerlinMidnight)
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", equalTo(emptyStatisticsDto(monthStart, berlinToday))));
    }

    @Test
    void ensureOverviewComputesAverageGrowthAndDistribution() throws Exception {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final OvertimeStatistic current = statisticOf(hours(3), hours(10), hours(20), hours(30));
        final OvertimeStatistic previous = statisticOf(hours(5));

        final LocalDate start = currentMonth.atDay(1);
        final LocalDate end = today;
        final LocalDate[] previousRange = toPreviousRange(start, end);

        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(start), toInstant(end))).thenReturn(current);
        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(previousRange[0]), toInstant(previousRange[1]))).thenReturn(previous);

        final CompanyStatisticsDto expected = new CompanyStatisticsDto(
            start, end,
            new OvertimeDurationDto(false, 15, 45),
            new OvertimeDurationDto(false, 10, 45),
            new OvertimeDistributionDto(4, List.of(
                new OvertimeDistributionEntryDto(0, 5, 1),
                new OvertimeDistributionEntryDto(5, 15, 1),
                new OvertimeDistributionEntryDto(15, 25, 1),
                new OvertimeDistributionEntryDto(25, null, 1)
            ))
        );

        perform(get("/web/company/overview").param("view", "month"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", equalTo(expected)));
    }

    private void stubCurrentAndPreviousRange(Person signedInUser, LocalDate start, LocalDate end, OvertimeStatistic statistic) {
        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(start), toInstant(end))).thenReturn(statistic);

        final LocalDate[] previousRange = toPreviousRange(start, end);
        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(previousRange[0]), toInstant(previousRange[1]))).thenReturn(statistic);
    }

    private static LocalDate[] toPreviousRange(LocalDate start, LocalDate end) {
        final long days = DAYS.between(start, end) + 1;
        final LocalDate previousEnd = start.minusDays(1);
        final LocalDate previousStart = previousEnd.minusDays(days - 1);
        return new LocalDate[]{previousStart, previousEnd};
    }

    private static Instant toInstant(LocalDate date) {
        return date.atStartOfDay().toInstant(UTC);
    }

    private static Duration hours(int value) {
        return Duration.ofHours(value);
    }

    private static OvertimeStatistic statisticOf(Duration... durationsByPerson) {
        final Map<PersonId, List<Overtime>> overtimesByPerson = new LinkedHashMap<>();
        for (int i = 0; i < durationsByPerson.length; i++) {
            final PersonId personId = new PersonId((long) i + 1);
            overtimesByPerson.put(personId, List.of(overtimeOf(personId, durationsByPerson[i])));
        }
        return new OvertimeStatistic(overtimesByPerson);
    }

    private static Overtime overtimeOf(PersonId personId, Duration duration) {
        final LocalDate day = LocalDate.of(2024, JANUARY, 1);
        return new Overtime(new OvertimeId(personId.value()), personId, new DateRange(day, day), duration, OvertimeType.UV_INTERNAL, Instant.EPOCH);
    }

    private static CompanyStatisticsDto emptyStatisticsDto(LocalDate from, LocalDate to) {
        return new CompanyStatisticsDto(
            from, to,
            new OvertimeDurationDto(false, 0, 0),
            new OvertimeDurationDto(false, 0, 0),
            new OvertimeDistributionDto(0, List.of(
                new OvertimeDistributionEntryDto(0, 5, 0),
                new OvertimeDistributionEntryDto(5, 15, 0),
                new OvertimeDistributionEntryDto(15, 25, 0),
                new OvertimeDistributionEntryDto(25, null, 0)
            ))
        );
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return perform(builder, sut);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder, CompanyController controller) throws Exception {
        return standaloneSetup(controller).build().perform(builder);
    }
}
