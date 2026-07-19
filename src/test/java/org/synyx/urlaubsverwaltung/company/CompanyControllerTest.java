package org.synyx.urlaubsverwaltung.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.search.PersonSearchUiFragmentSupplier;
import org.synyx.urlaubsverwaltung.search.PersonSuggestionUrlStrategy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.when;

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
    void ensureCompanyRedirectsToOverview() {
        assertThat(sut.company()).isEqualTo("redirect:/web/company/overview");
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
    void ensureOverviewDefaultsToMonthViewModeWhenViewParamIsAbsent() {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        stubCurrentAndPreviousRange(signedInUser, currentMonth.atDay(1), today, OvertimeStatistic.empty());

        final Model model = new ConcurrentModel();
        final String view = sut.overview(model, Optional.empty());

        assertThat(view).isEqualTo("company/company-overview");
        assertThat(model.getAttribute("viewMode")).isEqualTo("month");
    }

    @Test
    void ensureOverviewDefaultsToMonthViewModeWhenViewParamIsInvalid() {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        stubCurrentAndPreviousRange(signedInUser, currentMonth.atDay(1), today, OvertimeStatistic.empty());

        final Model model = new ConcurrentModel();
        final String view = sut.overview(model, Optional.of("not-a-view-mode"));

        assertThat(view).isEqualTo("company/company-overview");
        assertThat(model.getAttribute("viewMode")).isEqualTo("month");
    }

    @Test
    void ensureOverviewUsesQuarterDateRange() {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate start = currentMonth.minusMonths(2).atDay(1);
        stubCurrentAndPreviousRange(signedInUser, start, today, OvertimeStatistic.empty());

        final Model model = new ConcurrentModel();
        final String view = sut.overview(model, Optional.of("quarter"));

        assertThat(view).isEqualTo("company/company-overview");
        assertThat(model.getAttribute("viewMode")).isEqualTo("quarter");

        assertThat(model.getAttribute("statistics"))
            .asInstanceOf(type(CompanyStatisticsDto.class))
            .satisfies(statistics -> {
                assertThat(statistics.from()).isEqualTo(start);
                assertThat(statistics.to()).isEqualTo(today);
            });
    }

    @Test
    void ensureOverviewUsesYearDateRange() {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final LocalDate start = Year.of(currentMonth.getYear()).atDay(1);
        stubCurrentAndPreviousRange(signedInUser, start, today, OvertimeStatistic.empty());

        final Model model = new ConcurrentModel();
        final String view = sut.overview(model, Optional.of("year"));

        assertThat(view).isEqualTo("company/company-overview");
        assertThat(model.getAttribute("viewMode")).isEqualTo("year");

        assertThat(model.getAttribute("statistics"))
            .asInstanceOf(type(CompanyStatisticsDto.class))
            .satisfies(statistics -> {
                assertThat(statistics.from()).isEqualTo(start);
                assertThat(statistics.to()).isEqualTo(today);
            });
    }

    @Test
    void ensureOverviewComputesAverageGrowthAndDistribution() {

        final Person signedInUser = new Person();
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final OvertimeStatistic current = statisticOf(hours(3), hours(10), hours(20), hours(30));
        final OvertimeStatistic previous = statisticOf(hours(5));

        final LocalDate start = currentMonth.atDay(1);
        final LocalDate end = today;
        final LocalDate[] previousRange = toPreviousRange(start, end);

        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(start), toInstant(end))).thenReturn(current);
        when(overtimeStatisticService.getOvertimeStatistics(signedInUser, toInstant(previousRange[0]), toInstant(previousRange[1]))).thenReturn(previous);

        final Model model = new ConcurrentModel();
        sut.overview(model, Optional.of("month"));

        assertThat(model.getAttribute("statistics"))
            .asInstanceOf(type(CompanyStatisticsDto.class))
            .satisfies(statistics -> {
                assertThat(statistics.averageOvertime()).isEqualByComparingTo(BigDecimal.valueOf(15.75));
                assertThat(statistics.averageOvertimeGrowth()).isEqualByComparingTo(BigDecimal.valueOf(10.75));
                assertThat(statistics.overtimeDistribution().personCount()).isEqualTo(4);
                assertThat(statistics.overtimeDistribution().entries()).containsExactly(
                    new CompanyStatisticsDto.OvertimeDistributionEntry(0, 5, 1),
                    new CompanyStatisticsDto.OvertimeDistributionEntry(5, 15, 1),
                    new CompanyStatisticsDto.OvertimeDistributionEntry(15, 25, 1),
                    new CompanyStatisticsDto.OvertimeDistributionEntry(25, null, 1)
                );
            });
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
        final LocalDate day = LocalDate.of(2024, 1, 1);
        return new Overtime(new OvertimeId(personId.value()), personId, new DateRange(day, day), duration, OvertimeType.UV_INTERNAL, Instant.EPOCH);
    }
}
