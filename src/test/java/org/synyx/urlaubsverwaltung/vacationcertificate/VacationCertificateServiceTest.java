package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.MARCH;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.fullWorkday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendar;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToFriday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToSunday;

@ExtendWith(MockitoExtension.class)
class VacationCertificateServiceTest {

    private static final Year YEAR = Year.of(2026);
    private static final LocalDate FIRST_DAY = YEAR.atDay(1);
    private static final LocalDate LAST_DAY = FIRST_DAY.with(lastDayOfYear());
    private static final LocalDate EMPLOYMENT_TO = LocalDate.of(2026, JUNE, 30);

    private VacationCertificateService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private WorkingTimeCalendarService workingTimeCalendarService;
    @Mock
    private WorkingTimeService workingTimeService;

    @BeforeEach
    void setUp() {
        sut = new VacationCertificateService(applicationService, workingTimeCalendarService, workingTimeService);
    }

    @Nested
    class Entitlement {

        @Test
        void ensureEntitlementIsTheActualVacationDaysOfTheAccount() {

            final Account account = anyAccount(anyPerson());
            account.setAnnualVacationDays(BigDecimal.valueOf(30));
            account.setActualVacationDays(BigDecimal.valueOf(15));

            final VacationCertificate certificate = sut.getVacationCertificate(account, EMPLOYMENT_TO);

            assertThat(certificate.year()).isEqualTo(YEAR);
            assertThat(certificate.entitlement()).isEqualByComparingTo("15");
        }
    }

    @Nested
    class GrantedPeriods {

        @Test
        void ensureGrantedApplicationsAreListedSortedByStartDate() {

            final Person person = anyPerson();
            final Application march = application(person, LocalDate.of(2026, MARCH, 2), LocalDate.of(2026, MARCH, 3), FULL);
            final Application january = application(person, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 9), FULL);
            applications(person, List.of(march, january), List.of());
            calendar(person, workingTimeCalendarMondayToFriday(FIRST_DAY, LAST_DAY));

            final VacationCertificate certificate = sut.getVacationCertificate(anyAccount(person), EMPLOYMENT_TO);

            assertThat(certificate.grantedPeriods())
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(List.of(
                    new GrantedVacationPeriod(LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 9), FULL, new BigDecimal("5")),
                    new GrantedVacationPeriod(LocalDate.of(2026, MARCH, 2), LocalDate.of(2026, MARCH, 3), FULL, new BigDecimal("2"))
                ));
            assertThat(certificate.grantedTotal()).isEqualByComparingTo("7");
            assertThat(certificate.hasOpenApplications()).isFalse();
        }

        @Test
        void ensureOpenApplicationsAreFlagged() {

            final Person person = anyPerson();
            final Application waiting = application(person, LocalDate.of(2026, MARCH, 2), LocalDate.of(2026, MARCH, 3), FULL);
            applications(person, List.of(), List.of(waiting));

            final VacationCertificate certificate = sut.getVacationCertificate(anyAccount(person), EMPLOYMENT_TO);

            assertThat(certificate.grantedPeriods()).isEmpty();
            assertThat(certificate.grantedTotal()).isEqualByComparingTo("0");
            assertThat(certificate.hasOpenApplications()).isTrue();
        }

        @Test
        void ensureHalfDayCountsHalf() {

            final Person person = anyPerson();
            final LocalDate wednesday = LocalDate.of(2026, FEBRUARY, 4);
            applications(person, List.of(application(person, wednesday, wednesday, MORNING)), List.of());
            calendar(person, workingTimeCalendarMondayToFriday(FIRST_DAY, LAST_DAY));

            final VacationCertificate certificate = sut.getVacationCertificate(anyAccount(person), EMPLOYMENT_TO);

            assertThat(certificate.grantedPeriods()).singleElement().satisfies(period -> {
                assertThat(period.dayLength()).isEqualTo(MORNING);
                assertThat(period.days()).isEqualByComparingTo("0.5");
            });
            assertThat(certificate.grantedTotal()).isEqualByComparingTo("0.5");
        }

        @Test
        void ensurePublicHolidaysAreNotCounted() {

            final Person person = anyPerson();
            final LocalDate publicHoliday = LocalDate.of(2026, JANUARY, 6);
            final WorkingTimeCalendar calendar = workingTimeCalendar(FIRST_DAY, LAST_DAY, date -> date.equals(publicHoliday)
                ? new WorkingDayInformation(DayLength.ZERO, PUBLIC_HOLIDAY, PUBLIC_HOLIDAY)
                : fullWorkday());
            applications(person, List.of(application(person, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 9), FULL)), List.of());
            calendar(person, calendar);

            final VacationCertificate certificate = sut.getVacationCertificate(anyAccount(person), EMPLOYMENT_TO);

            assertThat(certificate.grantedTotal()).isEqualByComparingTo("4");
        }

        @Test
        void ensurePeriodAcrossTheTurnOfTheYearIsClippedToTheYear() {

            final Person person = anyPerson();
            final Application application = application(person, LocalDate.of(2025, DECEMBER, 29), LocalDate.of(2026, JANUARY, 2), FULL);
            applications(person, List.of(application), List.of());
            calendar(person, workingTimeCalendarMondayToFriday(FIRST_DAY, LAST_DAY));

            final VacationCertificate certificate = sut.getVacationCertificate(anyAccount(person), EMPLOYMENT_TO);

            assertThat(certificate.grantedPeriods()).singleElement().satisfies(period -> {
                assertThat(period.from()).isEqualTo(LocalDate.of(2026, JANUARY, 1));
                assertThat(period.to()).isEqualTo(LocalDate.of(2026, JANUARY, 2));
                assertThat(period.days()).isEqualByComparingTo("2");
            });
        }
    }

    @Nested
    class GrantedFromRemaining {

        @Test
        void ensureNothingIsTakenFromRemainingWithoutRemainingVacation() {
            final Account account = accountWithRemaining("0", "0", true);
            grant(account, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 9));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("0");
        }

        @Test
        void ensureDaysBeforeExpiryAreTakenFromRemaining() {
            final Account account = accountWithRemaining("5", "0", true);
            grant(account, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 7));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("3");
        }

        @Test
        void ensureAtMostTheRemainingVacationIsTakenFromRemaining() {
            final Account account = accountWithRemaining("5", "0", true);
            grant(account, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 14));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("5");
        }

        @Test
        void ensureDaysAfterExpiryAreTakenFromNotExpiringRemainingOnly() {
            final Account account = accountWithRemaining("5", "2", true);
            grant(account, LocalDate.of(2026, APRIL, 6), LocalDate.of(2026, APRIL, 10));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("2");
        }

        @Test
        void ensureDaysAfterExpiryAreNotTakenFromExpiredRemaining() {
            final Account account = accountWithRemaining("5", "0", true);
            grant(account, LocalDate.of(2026, APRIL, 6), LocalDate.of(2026, APRIL, 10));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("0");
        }

        @Test
        void ensureDaysAfterExpiryAreTakenFromRemainingThatDoesNotExpire() {
            final Account account = accountWithRemaining("5", "0", false);
            grant(account, LocalDate.of(2026, APRIL, 6), LocalDate.of(2026, APRIL, 10));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("5");
        }

        @Test
        void ensurePeriodAcrossTheExpiryDateIsSplit() {
            final Account account = accountWithRemaining("5", "0", true);
            // 30.3. + 31.3. before expiry, 1.4. - 3.4. after expiry
            grant(account, LocalDate.of(2026, MARCH, 30), LocalDate.of(2026, APRIL, 3));

            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("2");
        }

        @Test
        void ensureSeveralPeriodsBeforeAndAfterExpiryAreCombined() {
            final Account account = accountWithRemaining("5", "1", true);
            final Person person = account.getPerson();
            applications(person, List.of(
                application(person, LocalDate.of(2026, JANUARY, 5), LocalDate.of(2026, JANUARY, 6), FULL),
                application(person, LocalDate.of(2026, APRIL, 6), LocalDate.of(2026, APRIL, 8), FULL)
            ), List.of());
            calendar(person, workingTimeCalendarMondayToSunday(FIRST_DAY, LAST_DAY));

            // 2 days before expiry from remaining, 1 day after expiry from the not expiring remaining
            assertThat(sut.getVacationCertificate(account, EMPLOYMENT_TO).grantedFromRemaining()).isEqualByComparingTo("3");
        }

        private Account accountWithRemaining(String remaining, String notExpiring, boolean expire) {
            final Account account = anyAccount(anyPerson());
            account.setRemainingVacationDays(new BigDecimal(remaining));
            account.setRemainingVacationDaysNotExpiring(new BigDecimal(notExpiring));
            account.setDoRemainingVacationDaysExpireLocally(expire);
            return account;
        }

        private void grant(Account account, LocalDate from, LocalDate to) {
            final Person person = account.getPerson();
            applications(person, List.of(application(person, from, to, FULL)), List.of());
            calendar(person, workingTimeCalendarMondayToSunday(FIRST_DAY, LAST_DAY));
        }
    }

    private void applications(Person person, List<Application> granted, List<Application> open) {
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(FIRST_DAY, LAST_DAY, person, List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED), HOLIDAY))
            .thenReturn(granted);
        when(applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(FIRST_DAY, LAST_DAY, person, List.of(WAITING, TEMPORARY_ALLOWED), HOLIDAY))
            .thenReturn(open);
    }

    private void calendar(Person person, WorkingTimeCalendar calendar) {
        when(workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), YEAR)).thenReturn(Map.of(person, calendar));
    }

    private static Person anyPerson() {
        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        return person;
    }

    private static Account anyAccount(Person person) {
        final Account account = new Account();
        account.setPerson(person);
        account.setValidFrom(FIRST_DAY);
        account.setValidTo(LAST_DAY);
        account.setExpiryDateLocally(LocalDate.of(2026, APRIL, 1));
        account.setAnnualVacationDays(BigDecimal.valueOf(30));
        account.setActualVacationDays(BigDecimal.valueOf(30));
        account.setRemainingVacationDays(ZERO);
        account.setRemainingVacationDaysNotExpiring(ZERO);
        account.setDoRemainingVacationDaysExpireGlobally(true);
        return account;
    }

    private static Application application(Person person, LocalDate from, LocalDate to, DayLength dayLength) {
        final Application application = new Application();
        application.setId(1L);
        application.setPerson(person);
        application.setVacationType(createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setStartDate(from);
        application.setEndDate(to);
        application.setDayLength(dayLength);
        application.setStatus(ALLOWED);
        return application;
    }
}
