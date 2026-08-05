package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeId;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.Duration.ZERO;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Comparator.naturalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeType.UV_INTERNAL;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class OvertimeStatisticsServiceTest {

    private static final Year YEAR = Year.of(2026);

    private OvertimeStatisticsService sut;

    @Mock
    private OvertimeService overtimeService;
    @Mock
    private PersonService personService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeStatisticsService(overtimeService, personService, applicationService, workingTimeCalendarService);

        // most tests are about the overtime records, so "no reduction applications" is the default
        lenient().when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of());
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

    @Test
    void ensureOvertimeReductionApplicationsCountAsReduction() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();

        // 05.01. and 06.01., two full workdays, eight hours reduction means four hours per day
        overtimeReductionApplications(marie, application(marie, "2026-01-05", "2026-01-06", Duration.ofHours(8)));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth().get(0)).isEqualTo(Duration.ofHours(8));
        assertThat(statistics.accruedByMonth().get(0)).isEqualTo(ZERO);
    }

    @Test
    void ensureReductionCombinesNegativeRecordsAndApplications() {

        final Person marie = person(1L);
        personsHavingAccount(marie);

        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of(
            marie.getIdAsPersonId(), List.of(overtime(marie, "2026-01-20", "2026-01-20", Duration.ofHours(3).negated()))
        ));
        overtimeReductionApplications(marie, application(marie, "2026-01-05", "2026-01-05", Duration.ofHours(2)));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth().get(0)).isEqualTo(Duration.ofHours(5));
    }

    @Test
    void ensureOnlyApplicationsInActiveStatusesAreConsidered() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();
        overtimeReductionApplications(marie);

        sut.getStatistics(YEAR);

        final ArgumentCaptor<List<ApplicationStatus>> captor = ArgumentCaptor.captor();
        verify(applicationService).getForStatesAndPerson(captor.capture(), any(), any(), any());
        assertThat(captor.getValue()).containsExactlyInAnyOrderElementsOf(activeStatuses());
    }

    @Test
    void ensureApplicationsOfOtherVacationCategoriesAreIgnored() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();

        final Application holiday = application(marie, "2026-01-05", "2026-01-06", Duration.ofHours(8));
        holiday.setVacationType(createVacationType(2L, HOLIDAY, new StaticMessageSource()));
        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(holiday));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth()).containsOnly(ZERO);
    }

    @Test
    void ensureApplicationSpanningTwoMonthsIsSplitProRata() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();

        // 30.01. - 02.02., four full workdays, eight hours means two hours per day
        overtimeReductionApplications(marie, application(marie, "2026-01-30", "2026-02-02", Duration.ofHours(8)));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth().get(0)).isEqualTo(Duration.ofHours(4));
        assertThat(statistics.reductionByMonth().get(1)).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void ensureApplicationSpanningTheYearBoundaryOnlyCountsItsShareOfTheSelectedYear() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();

        // 30.12.2025 - 02.01.2026, four full workdays, only the two days in 2026 belong to the selected year
        overtimeReductionApplications(marie, application(marie, "2025-12-30", "2026-01-02", Duration.ofHours(8)));

        final OvertimeStatistics statistics = sut.getStatistics(YEAR);

        assertThat(statistics.reductionByMonth().get(0)).isEqualTo(Duration.ofHours(4));
        assertThat(statistics.reduction()).isEqualTo(Duration.ofHours(4));
    }

    @Test
    void ensureWorkingTimeCalendarsAreLoadedOnceForTheWholeSpanOfTheApplications() {

        final Person marie = person(1L);
        final Person klaus = person(2L);
        personsHavingAccount(marie, klaus);
        noOvertimeRecords();

        overtimeReductionApplications(
            marie,
            application(marie, "2025-12-30", "2026-01-02", Duration.ofHours(8)),
            application(marie, "2026-11-02", "2026-11-03", Duration.ofHours(4))
        );

        sut.getStatistics(YEAR);

        // one query for everyone, spanning every application - not one per person and not one per application
        verify(workingTimeCalendarService).getWorkingTimesByPersons(
            any(),
            eq(new DateRange(LocalDate.parse("2025-12-30"), LocalDate.parse("2026-11-03")))
        );
    }

    @Test
    void ensureNoWorkingTimeCalendarIsLoadedWithoutAnyReductionApplication() {

        final Person marie = person(1L);
        personsHavingAccount(marie);
        noOvertimeRecords();
        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of());

        sut.getStatistics(YEAR);

        verify(workingTimeCalendarService, never()).getWorkingTimesByPersons(any(), any(DateRange.class));
    }

    @Nested
    class GetTotals {

        @Test
        void ensureAccrualAndReductionOverTheWholeHistory() {

            final Person marie = person(1L);
            final Person klaus = person(2L);
            when(personService.getActivePersons()).thenReturn(List.of(marie, klaus));

            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of(
                marie.getIdAsPersonId(), List.of(
                    overtime(marie, "2019-04-02", "2019-04-02", Duration.ofHours(40)),
                    overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(6).negated())
                ),
                klaus.getIdAsPersonId(), List.of(overtime(klaus, "2024-11-11", "2024-11-11", Duration.ofHours(10)))
            ));
            when(applicationService.getTotalOvertimeReductionOfPersons(any())).thenReturn(Duration.ofHours(4));

            final OvertimeTotals totals = sut.getTotals();

            assertThat(totals.accrued()).isEqualTo(Duration.ofHours(50));
            assertThat(totals.reduction()).isEqualTo(Duration.ofHours(10));
            assertThat(totals.balance()).isEqualTo(Duration.ofHours(40));
        }

        @Test
        void ensureReductionApplicationsAreCountedWithoutAnyDateRestriction() {

            final Person marie = person(1L);
            when(personService.getActivePersons()).thenReturn(List.of(marie));
            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of());
            when(applicationService.getTotalOvertimeReductionOfPersons(List.of(marie))).thenReturn(Duration.ofHours(3));

            assertThat(sut.getTotals().reduction()).isEqualTo(Duration.ofHours(3));
        }

        @Test
        void ensureTheCurrentWorkforceIsUsedAndNotTheCohortOfAnyYear() {

            final Person marie = person(1L);
            when(personService.getActivePersons()).thenReturn(List.of(marie));
            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of());
            when(applicationService.getTotalOvertimeReductionOfPersons(any())).thenReturn(ZERO);

            sut.getTotals();

            verify(personService, never()).getAllPersonsHavingAccountInYear(any());
        }

        @Test
        void ensureEverythingIsFetchedInOneQueryPerSource() {

            final Person marie = person(1L);
            final Person klaus = person(2L);
            when(personService.getActivePersons()).thenReturn(List.of(marie, klaus));
            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of());
            when(applicationService.getTotalOvertimeReductionOfPersons(any())).thenReturn(ZERO);

            sut.getTotals();

            verify(overtimeService).getAllOvertimesByPersonIds(List.of(marie.getIdAsPersonId(), klaus.getIdAsPersonId()));
            verify(applicationService).getTotalOvertimeReductionOfPersons(List.of(marie, klaus));
        }

        @Test
        void ensureEmptyCompanyIsZeroEverywhereWithoutQueryingAnything() {

            when(personService.getActivePersons()).thenReturn(List.of());

            final OvertimeTotals totals = sut.getTotals();

            assertThat(totals.accrued()).isEqualTo(ZERO);
            assertThat(totals.reduction()).isEqualTo(ZERO);
            assertThat(totals.balance()).isEqualTo(ZERO);

            verify(overtimeService, never()).getAllOvertimesByPersonIds(any());
            verify(applicationService, never()).getTotalOvertimeReductionOfPersons(any());
        }

        @Test
        void ensureBalanceIsNegativeWhenMoreWasReducedThanAccrued() {

            final Person marie = person(1L);
            when(personService.getActivePersons()).thenReturn(List.of(marie));
            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of(
                marie.getIdAsPersonId(), List.of(overtime(marie, "2026-01-05", "2026-01-05", Duration.ofHours(2)))
            ));
            when(applicationService.getTotalOvertimeReductionOfPersons(any())).thenReturn(Duration.ofHours(9));

            assertThat(sut.getTotals().balance()).isEqualTo(Duration.ofHours(7).negated());
        }

        /**
         * The balance shown to office and boss has to be the same figure every person sees as their own remaining
         * overtime. That identity is what makes the number trustworthy, so it is pinned down here.
         */
        @Test
        void ensureBalanceEqualsAllOvertimeRecordsMinusAllReductionApplications() {

            final Person marie = person(1L);
            when(personService.getActivePersons()).thenReturn(List.of(marie));

            final List<Overtime> records = List.of(
                overtime(marie, "2024-02-01", "2024-02-01", Duration.ofHours(12)),
                overtime(marie, "2024-06-01", "2024-06-01", Duration.ofHours(5).negated()),
                overtime(marie, "2025-09-01", "2025-09-01", Duration.ofHours(8))
            );
            when(overtimeService.getAllOvertimesByPersonIds(any())).thenReturn(Map.of(marie.getIdAsPersonId(), records));
            when(applicationService.getTotalOvertimeReductionOfPersons(any())).thenReturn(Duration.ofHours(6));

            // getLeftOvertimeForPerson is the sum of all records minus the reduction applications
            final Duration sumOfAllRecords = records.stream().map(Overtime::duration).reduce(ZERO, Duration::plus);
            final Duration leftOvertime = sumOfAllRecords.minus(Duration.ofHours(6));

            assertThat(sut.getTotals().balance()).isEqualTo(leftOvertime);
        }
    }

    private void personsHavingAccount(Person... persons) {
        when(personService.getAllPersonsHavingAccountInYear(YEAR)).thenReturn(List.of(persons));
    }

    private void noOvertimeRecords() {
        when(overtimeService.getOvertimeForPersonsInDateRange(any(), any(), any())).thenReturn(Map.of());
    }

    private void overtimeReductionApplications(Person person, Application... applications) {
        when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of(applications));

        if (applications.length > 0) {
            final LocalDate from = Stream.of(applications).map(Application::getStartDate).min(naturalOrder()).orElseThrow();
            final LocalDate to = Stream.of(applications).map(Application::getEndDate).max(naturalOrder()).orElseThrow();
            when(workingTimeCalendarService.getWorkingTimesByPersons(any(), any(DateRange.class)))
                .thenReturn(Map.of(person, workingTimeCalendarMondayToSunday(from, to)));
        }
    }

    private static Application application(Person person, String start, String end, Duration hours) {
        final Application application = new Application();
        application.setPerson(person);
        application.setStartDate(LocalDate.parse(start));
        application.setEndDate(LocalDate.parse(end));
        application.setDayLength(FULL);
        application.setStatus(ALLOWED);
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        application.setHours(hours);
        return application;
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
