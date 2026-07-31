package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ZERO;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.UV_INTERNAL;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsServiceTest {

    private static final Year YEAR = Year.of(2026);

    private OvertimeStatisticsService sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsService(overtimeService, personService);
    }

    @Test
    void ensureAccruedOvertimeIsSummedPerMonthOverAllPersons() {

        final Person marie = person(1L);
        final Person klaus = person(2L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie, klaus));

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(3))),
            klaus.getIdAsPersonId(), List.of(
                overtime(klaus, "2026-01-07", "2026-01-07", Duration.ofHours(2)),
                overtime(klaus, "2026-03-09", "2026-03-09", Duration.ofHours(4))
            )
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accruedByMonth()).hasSize(12);
        assertThat(statistics.accruedByMonth().get(JANUARY.getValue() - 1)).isEqualTo(Duration.ofHours(5));
        assertThat(statistics.accruedByMonth().get(MARCH.getValue() - 1)).isEqualTo(Duration.ofHours(4));
        assertThat(statistics.accruedByMonth().get(FEBRUARY.getValue() - 1)).isEqualTo(ZERO);
    }

    @Test
    void ensureNegativeOvertimeRecordsCountAsReductionAndNotAsAccrual() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(
                overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(6)),
                overtime(marie, "2026-01-20", "2026-01-20", Duration.ofHours(2).negated())
            )
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accruedByMonth().get(0)).isEqualTo(Duration.ofHours(6));
        assertThat(statistics.reductionByMonth().get(0)).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void ensureReductionIsReportedAsPositiveAmount() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(overtime(marie, "2026-02-02", "2026-02-02", Duration.ofHours(3).negated()))
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth().get(1)).isEqualTo(Duration.ofHours(3));
        assertThat(statistics.reductionByMonth().get(1).isNegative()).isFalse();
    }

    @Test
    void ensureOvertimeSpanningTwoMonthsIsSplitProRata() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        // 30.01. - 02.02. are four days, two in january and two in february
        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(overtime(marie, "2026-01-30", "2026-02-02", Duration.ofHours(4)))
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accruedByMonth().get(0)).isEqualTo(Duration.ofHours(2));
        assertThat(statistics.accruedByMonth().get(1)).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void ensureOvertimeSpanningTheYearBoundaryOnlyCountsItsShareOfTheSelectedYear() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        // 30.12.2025 - 02.01.2026 are four days, only the two days in 2026 belong to the selected year
        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(overtime(marie, "2025-12-30", "2026-01-02", Duration.ofHours(8)))
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accruedByMonth().get(0)).isEqualTo(Duration.ofHours(4));
        assertThat(statistics.accrued()).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void ensureOnlyPersonsHavingAnAccountInTheSelectedYearAreConsidered() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));
        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of());

        sut.getStatistics(YEAR);

        final ArgumentCaptor<Collection<PersonId>> captor = ArgumentCaptor.captor();
        verify(overtimeService).getOvertimeForPersonsInDateRange(captor.capture(), any(), any());
        assertThat(captor.getValue()).containsExactly(marie.getIdAsPersonId());
    }

    @Test
    void ensureOvertimeIsRequestedForTheWholeSelectedYear() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));
        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of());

        sut.getStatistics(YEAR);

        verify(overtimeService).getOvertimeForPersonsInDateRange(
            any(),
            eq(Instant.parse("2026-01-01T00:00:00Z")),
            eq(Instant.parse("2026-12-31T00:00:00Z"))
        );
    }

    @Test
    void ensureNoPersonsResultsInZeroForEveryMonthWithoutQueryingOvertime() {

        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of());

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accruedByMonth()).hasSize(12).containsOnly(ZERO);
        assertThat(statistics.reductionByMonth()).hasSize(12).containsOnly(ZERO);
        assertThat(statistics.accrued()).isEqualTo(ZERO);
        assertThat(statistics.reduction()).isEqualTo(ZERO);
        assertThat(statistics.balance()).isEqualTo(ZERO);

        verify(overtimeService, never()).getOvertimeForPersonsInDateRange(any(), any(), any());
    }

    @Test
    void ensureTotalsAndBalance() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(
                overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(10)),
                overtime(marie, "2026-06-05", "2026-06-05", Duration.ofHours(4).negated())
            )
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.accrued()).isEqualTo(Duration.ofHours(10));
        assertThat(statistics.reduction()).isEqualTo(Duration.ofHours(4));
        assertThat(statistics.balance()).isEqualTo(Duration.ofHours(6));
    }

    @Test
    void ensureBalancePerMonthIsAccrualMinusReduction() {

        final Person marie = person(1L);
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(marie));

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(
                overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(2)),
                overtime(marie, "2026-01-06", "2026-01-06", Duration.ofHours(5).negated())
            )
        ));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.balanceByMonth().get(0)).isEqualTo(Duration.ofHours(3).negated());
    }

    private static Person person(long id) {
        final Person person = new Person("user-" + id, "Reichenbach", "Marie", "person%d@example.org".formatted(id));
        person.setId(id);
        return person;
    }

    private static Overtime overtime(Person person, String start, String end, Duration duration) {
        return new Overtime(
            new OvertimeId(1L),
            person.getIdAsPersonId(),
            new DateRange(LocalDate.parse(start), LocalDate.parse(end)),
            duration,
            UV_INTERNAL,
            Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
