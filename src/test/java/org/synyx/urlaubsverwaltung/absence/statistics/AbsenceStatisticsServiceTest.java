package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodService;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriod;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@ExtendWith(MockitoExtension.class)
class AbsenceStatisticsServiceTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);

    private AbsenceStatisticsService sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonActivePeriodService personActivePeriodService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private AccountService accountService;
    @Mock
    private VacationDaysService vacationDaysService;

    @Captor
    private ArgumentCaptor<List<Person>> personsCaptor;

    private static Person person(long id, org.synyx.urlaubsverwaltung.person.Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private static Account account(long id, BigDecimal actualVacationDays, BigDecimal remainingVacationDays,
                                    BigDecimal remainingVacationDaysNotExpiring, LocalDate expiryDate) {
        final Account account = new Account(new Person(), LocalDate.now(), LocalDate.now(), true, expiryDate,
            BigDecimal.ZERO, remainingVacationDays, remainingVacationDaysNotExpiring, "");
        account.setId(id);
        account.setActualVacationDays(actualVacationDays);
        return account;
    }

    private static VacationDaysLeft vacationDaysLeft(Account account) {
        return VacationDaysLeft.builder()
            .withAnnualVacation(account.getActualVacationDays())
            .withRemainingVacation(account.getRemainingVacationDays())
            .notExpiring(account.getRemainingVacationDaysNotExpiring())
            .build();
    }

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);
        sut = new AbsenceStatisticsService(personService, departmentService, personActivePeriodService,
            applicationService, workingTimeCalendarService, accountService, vacationDaysService, clock);
    }

    private void givenOfficePersons(Person signedInUser, List<Person> allPersons) {
        when(personService.getAllPersons()).thenReturn(allPersons);
        final Map<PersonId, List<PersonActivePeriod>> activePeriods = allPersons.stream()
            .collect(java.util.stream.Collectors.toMap(Person::getIdAsPersonId,
                p -> List.of(new PersonActivePeriod(p.getIdAsPersonId(), Instant.EPOCH))));
        when(personActivePeriodService.getActivePeriodsOverlapping(any(), any(), any())).thenReturn(activePeriods);
    }

    @Nested
    class Wiring {

        @Test
        void applicationsAreRequestedForTheWholeYearWithActiveStatuses() {

            final Person signedInUser = person(1, OFFICE);
            final Person member = person(2);
            givenOfficePersons(signedInUser, List.of(member));

            when(applicationService.getApplicationsForACertainPeriodAndStatus(any(LocalDate.class), any(LocalDate.class), anyList(), anyList())).thenReturn(List.of());
            when(workingTimeCalendarService.getWorkingTimesByPersons(any(), any(Year.class))).thenReturn(Map.of());
            when(accountService.getHolidaysAccount(2024, List.of(member))).thenReturn(List.of());
            when(vacationDaysService.getVacationDaysLeft(eq(List.of()), any(DateRange.class), eq(List.of()), any())).thenReturn(Map.of());

            sut.createStatistics(Year.of(2024), signedInUser);

            verify(applicationService).getApplicationsForACertainPeriodAndStatus(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), List.of(member), activeStatuses());
        }

        @Test
        void personsComeFromAbsenceStatisticsPersons() {

            final Person signedInUser = person(1, OFFICE);
            final Person memberA = person(2);
            final Person memberB = person(3);
            givenOfficePersons(signedInUser, List.of(memberA, memberB));

            when(applicationService.getApplicationsForACertainPeriodAndStatus(any(LocalDate.class), any(LocalDate.class), anyList(), anyList())).thenReturn(List.of());
            when(workingTimeCalendarService.getWorkingTimesByPersons(any(), any(Year.class))).thenReturn(Map.of());
            when(accountService.getHolidaysAccount(eq(2024), personsCaptor.capture())).thenReturn(List.of());
            when(vacationDaysService.getVacationDaysLeft(eq(List.of()), any(DateRange.class), eq(List.of()), any())).thenReturn(Map.of());

            sut.createStatistics(Year.of(2024), signedInUser);

            assertThat(personsCaptor.getValue()).containsExactlyInAnyOrder(memberA, memberB);
            verify(workingTimeCalendarService).getWorkingTimesByPersons(List.of(memberA, memberB), Year.of(2024));
        }

        @Test
        void emptyPersonsYieldsAnEmptyResultWithoutFurtherLookups() {

            final Person signedInUser = person(1); // no eligible role -> empty person set

            final AbsenceStatistics actual = sut.createStatistics(Year.of(2024), signedInUser);

            assertThat(actual.monthlyAbsenceDaysByType()).isEmpty();
            assertThat(actual.vacationDaysTaken().vacationDaysTaken()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(actual.vacationDaysTaken().percentage()).isEqualByComparingTo(BigDecimal.ZERO);
            verifyNoInteractions(applicationService, workingTimeCalendarService, accountService, vacationDaysService);
        }
    }

    @Nested
    class StichtagRule {

        @Test
        void currentYearUsesToday() {

            // expiry after "today" (2024-06-15) but before the year's own Dec 31 -> distinguishes "today" from
            // the past-year rule, which would use Dec 31 and thus see it as already expired
            final LocalDate expiryDate = LocalDate.of(2024, 12, 1);
            final Account account = account(1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, expiryDate);
            final Person member = person(2);
            final Person signedInUser = person(1, OFFICE);
            givenOfficePersons(signedInUser, List.of(member));

            givenAccountsAndCalendars(member, account);

            final AbsenceStatistics actual = sut.createStatistics(Year.of(2024), signedInUser);

            assertThat(actual.vacationDaysTaken().expiredRemainingVacationDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void pastYearUsesDecemberThirtyFirstOfThatYear() {

            // expiry between Dec 31, 2023 (correct stichtag) and "today" 2024-06-15 (wrong, if today were used
            // instead) -> distinguishes the two
            final LocalDate expiryDate = LocalDate.of(2024, 1, 15);
            final Account account = account(1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, expiryDate);
            final Person member = person(2);
            final Person signedInUser = person(1, OFFICE);
            givenOfficePersons(signedInUser, List.of(member));

            givenAccountsAndCalendars(member, account);

            final AbsenceStatistics actual = sut.createStatistics(Year.of(2023), signedInUser);

            // Dec 31, 2023 is before the 2024-01-15 expiry -> not yet expired
            assertThat(actual.vacationDaysTaken().expiredRemainingVacationDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void futureYearUsesJanuaryFirstOfThatYear() {

            // expiry between Jan 1, 2025 (correct stichtag) and Dec 31, 2025 (wrong, if the past-year rule were
            // mistakenly applied) -> distinguishes the two
            final LocalDate expiryDate = LocalDate.of(2025, 6, 1);
            final Account account = account(1, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, expiryDate);
            final Person member = person(2);
            final Person signedInUser = person(1, OFFICE);
            givenOfficePersons(signedInUser, List.of(member));

            givenAccountsAndCalendars(member, account);

            final AbsenceStatistics actual = sut.createStatistics(Year.of(2025), signedInUser);

            // Jan 1, 2025 is before the 2025-06-01 expiry -> not yet expired
            assertThat(actual.vacationDaysTaken().expiredRemainingVacationDays()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        private void givenAccountsAndCalendars(Person member, Account account) {
            when(applicationService.getApplicationsForACertainPeriodAndStatus(any(LocalDate.class), any(LocalDate.class), anyList(), anyList())).thenReturn(List.of());
            when(workingTimeCalendarService.getWorkingTimesByPersons(any(), any(Year.class))).thenReturn(Map.of());
            when(accountService.getHolidaysAccount(anyInt(), anyList())).thenReturn(List.of(account));
            when(vacationDaysService.getVacationDaysLeft(eq(List.of(account)), any(DateRange.class), eq(List.of()), any()))
                .thenReturn(Map.of(account, new HolidayAccountVacationDays(account, vacationDaysLeft(account), vacationDaysLeft(account))));
        }
    }
}
