package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.synyx.urlaubsverwaltung.QueryCountTestSupport;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

/**
 * Guards the holidays account jobs against N+1 queries: the number of statements must not depend on the number of
 * persons. Today is 2026-05-04 (see {@link QueryCountTestSupport}).
 */
class AccountJobsQueryCountIT extends QueryCountTestSupport {

    /**
     * The turn of the year saves every created holidays account on its own, the values differ per account. Per
     * account: the insert of the account and the two settings reads of {@code AccountServiceImpl.save} (global expiry
     * date and whether remaining vacation days expire).
     */
    private static final int STATEMENTS_PER_CREATED_ACCOUNT = 3;

    @Autowired
    private VacationDaysReminderService vacationDaysReminderService;
    @Autowired
    private TurnOfTheYearAccountUpdaterService turnOfTheYearAccountUpdaterService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeWriteService workingTimeWriteService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private VacationTypeService vacationTypeService;

    @Test
    void remindForCurrentlyLeftVacationDaysDoesNotGrowWithPersons() {
        final Measurement small = seedAndMeasure(1, List.of(2025, 2026, 2027), vacationDaysReminderService::remindForCurrentlyLeftVacationDays);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, List.of(2025, 2026, 2027), vacationDaysReminderService::remindForCurrentlyLeftVacationDays);

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isEqualTo(small.statements());
    }

    @Test
    void notifyForExpiredRemainingVacationDaysDoesNotGrowWithPersons() {
        final Measurement small = seedAndMeasure(1, List.of(2025, 2026, 2027), vacationDaysReminderService::notifyForExpiredRemainingVacationDays);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, List.of(2025, 2026, 2027), vacationDaysReminderService::notifyForExpiredRemainingVacationDays);

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isEqualTo(small.statements());
    }

    @Test
    void updateAccountsForNextPeriodGrowsOnlyBySavingTheCreatedAccounts() {
        // accounts of last year only: the job creates the accounts of this year, as it does on the 1st of January
        final Measurement small = seedAndMeasure(1, List.of(2025), turnOfTheYearAccountUpdaterService::updateAccountsForNextPeriod);
        truncatePersonData();
        final Measurement large = seedAndMeasure(4, List.of(2025), turnOfTheYearAccountUpdaterService::updateAccountsForNextPeriod);

        // 3 departments with 3 additional members each
        final int additionalPersons = 9;

        assertThat(small.mails()).isPositive();
        assertThat(large.mails()).isGreaterThan(small.mails());
        assertThat(large.statements()).isLessThanOrEqualTo(small.statements() + STATEMENTS_PER_CREATED_ACCOUNT * additionalPersons);
    }

    private Measurement seedAndMeasure(int membersPerDepartment, List<Integer> accountYears, Runnable job) {

        final Staff staff = createStaff(membersPerDepartment);
        for (Person person : staff.all()) {
            workingTimeWriteService.touch(List.of(1, 2, 3, 4, 5), LocalDate.of(2025, 1, 1), person);
            accountYears.forEach(year -> createAccount(person, Year.of(year)));
        }

        final VacationType<?> holiday = vacationTypeService.getActiveVacationTypes().stream()
            .filter(vacationType -> vacationType.isOfCategory(HOLIDAY))
            .findFirst()
            .orElseThrow();
        for (Person member : staff.members()) {
            final Application application = new Application();
            application.setPerson(member);
            application.setApplier(member);
            application.setBoss(staff.boss());
            application.setVacationType(holiday);
            application.setDayLength(FULL);
            application.setStartDate(LocalDate.of(2026, 2, 2));
            application.setEndDate(LocalDate.of(2026, 2, 3));
            application.setApplicationDate(LocalDate.of(2026, 1, 15));
            application.setStatus(ALLOWED);
            applicationService.save(application);
        }

        return measure(job);
    }

    private void createAccount(Person person, Year year) {
        final LocalDate firstDayOfYear = year.atDay(1);
        accountInteractionService.updateOrCreateHolidaysAccount(
            person, firstDayOfYear, firstDayOfYear.withMonth(12).withDayOfMonth(31),
            true, firstDayOfYear.withMonth(4),
            BigDecimal.valueOf(30), BigDecimal.valueOf(30), BigDecimal.valueOf(5), ZERO, "query count"
        );
    }
}
