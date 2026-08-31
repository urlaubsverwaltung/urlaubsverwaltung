package org.synyx.urlaubsverwaltung.absence.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.account.HolidayAccountVacationDays;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.account.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;

/**
 * Fetches the data behind the absence statistics page and assembles it, via {@link MonthlyAbsenceDays} and
 * {@link VacationDaysTaken}, into an {@link AbsenceStatistics} for a single year.
 */
@Service
@Transactional(readOnly = true)
public class AbsenceStatisticsService {

    private final AbsenceStatisticsPersons absenceStatisticsPersons;
    private final ApplicationService applicationService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final Clock clock;

    AbsenceStatisticsService(
        AbsenceStatisticsPersons absenceStatisticsPersons,
        ApplicationService applicationService,
        WorkingTimeCalendarService workingTimeCalendarService,
        AccountService accountService,
        VacationDaysService vacationDaysService,
        Clock clock
    ) {
        this.absenceStatisticsPersons = absenceStatisticsPersons;
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.clock = clock;
    }

    /**
     * Creates the {@link AbsenceStatistics} for the given year, from the perspective of the signed-in person.
     *
     * @param year         year to create the statistics for
     * @param signedInUser person requesting the statistics
     * @return the assembled statistics; empty but exception-free when nobody is relevant for the given person/year
     */
    public AbsenceStatistics createStatistics(Year year, Person signedInUser) {

        final List<Person> persons = absenceStatisticsPersons.relevantPersons(signedInUser, year);
        if (persons.isEmpty()) {
            return new AbsenceStatistics(year, Map.of(), VacationDaysTaken.calculate(asOfDate(year), Map.of()));
        }

        final DateRange yearRange = DateRange.ofYear(year);

        final List<Application> applications =
            applicationService.getApplicationsForACertainPeriodAndStatus(yearRange.startDate(), yearRange.endDate(), persons, activeStatuses());

        // computed once and threaded through both collaborators below, instead of letting each of them recompute
        // the same day-by-day calendar for the same persons and year - ApplicationForLeaveStatisticsBuilder does
        // the same and explains why in its own comment.
        final Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson = workingTimeCalendarService.getWorkingTimesByPersons(persons, year);

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> monthlyAbsenceDaysByType =
            MonthlyAbsenceDays.calculate(year, applications, workingTimeCalendarsByPerson);

        final List<Account> accounts = accountService.getHolidaysAccount(year.getValue(), persons);

        // next year's accounts carry how much of *this* year's entitlement has already been booked into next
        // January. Without them VacationDaysLeft#vacationDaysUsedNextYear stays zero, the left days come out too
        // high and the taken days therefore too low, understating how much vacation is still open.
        // OverviewViewController passes them for the same reason.
        final List<Account> accountsNextYear = accountService.getHolidaysAccount(year.getValue() + 1, persons);

        final Map<Account, HolidayAccountVacationDays> vacationDaysLeftByAccount =
            vacationDaysService.getVacationDaysLeft(accounts, yearRange, accountsNextYear, workingTimeCalendarsByPerson);
        final Map<Account, VacationDaysLeft> vacationDaysLeftYearByAccount = vacationDaysLeftByAccount.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().vacationDaysYear()));

        final VacationDaysTakenResult vacationDaysTakenResult = VacationDaysTaken.calculate(asOfDate(year), vacationDaysLeftYearByAccount);

        return new AbsenceStatistics(year, monthlyAbsenceDaysByType, vacationDaysTakenResult);
    }

    /**
     * The as-of date {@link VacationDaysTaken} is evaluated at: today for the current year, the year's own Dec 31st
     * for a past year, and the year's own Jan 1st for a future year.
     */
    private LocalDate asOfDate(Year year) {

        final Year currentYear = Year.now(clock);

        if (year.equals(currentYear)) {
            return LocalDate.now(clock);
        }
        if (year.isBefore(currentYear)) {
            return year.atDay(year.length());
        }
        return year.atDay(1);
    }
}
