package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.Comparator.comparing;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;

/**
 * Computes the vacation certificate (§ 6 Abs. 2 BUrlG) of a person for the year of a holiday account.
 */
@Service
class VacationCertificateService {

    private static final List<ApplicationStatus> GRANTED_STATUSES = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
    private static final List<ApplicationStatus> OPEN_STATUSES = List.of(WAITING, TEMPORARY_ALLOWED);

    private final ApplicationService applicationService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final WorkingTimeService workingTimeService;

    VacationCertificateService(
        ApplicationService applicationService,
        WorkingTimeCalendarService workingTimeCalendarService,
        WorkingTimeService workingTimeService
    ) {
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.workingTimeService = workingTimeService;
    }

    /**
     * @param account      holiday account of the person and year of the certificate
     * @param employmentTo last day of the employment
     * @return the vacation certificate of the account's person and year
     */
    VacationCertificate getVacationCertificate(Account account, LocalDate employmentTo) {

        final Person person = account.getPerson();
        final Year year = Year.of(account.getYear());
        final DateRange yearRange = DateRange.ofYear(year);

        final List<Application> grantedApplications = holidayApplications(person, yearRange, GRANTED_STATUSES).stream()
            .sorted(comparing(Application::getStartDate))
            .toList();
        final boolean hasOpenApplications = !holidayApplications(person, yearRange, OPEN_STATUSES).isEmpty();

        final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarService.getWorkingTimesByPersons(List.of(person), year).get(person);

        final List<GrantedVacationPeriod> grantedPeriods = grantedApplications.stream()
            .map(application -> grantedPeriod(application, yearRange, workingTimeCalendar))
            .toList();
        final BigDecimal grantedTotal = grantedPeriods.stream()
            .map(GrantedVacationPeriod::days)
            .reduce(ZERO, BigDecimal::add);

        final BigDecimal grantedFromRemaining = grantedFromRemaining(account, grantedApplications, yearRange, workingTimeCalendar);

        return new VacationCertificate(year, account.getActualVacationDays(), Optional.empty(), grantedPeriods,
            grantedTotal, grantedFromRemaining, hasOpenApplications);
    }

    private List<Application> holidayApplications(Person person, DateRange yearRange, List<ApplicationStatus> statuses) {
        return applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(
            yearRange.startDate(), yearRange.endDate(), person, statuses, HOLIDAY);
    }

    private static GrantedVacationPeriod grantedPeriod(Application application, DateRange yearRange, WorkingTimeCalendar workingTimeCalendar) {
        final LocalDate from = application.getStartDate().isBefore(yearRange.startDate()) ? yearRange.startDate() : application.getStartDate();
        final LocalDate to = application.getEndDate().isAfter(yearRange.endDate()) ? yearRange.endDate() : application.getEndDate();
        final BigDecimal days = workingTimeCalendar.workingTimeInDateRage(application, yearRange);
        return new GrantedVacationPeriod(from, to, application.getDayLength(), days);
    }

    /**
     * Uses the remaining vacation the same way the holiday account does: days before the expiry date use the remaining
     * vacation, days after the expiry date only the part of it that does not expire, everything else the entitlement of
     * the year. Unlike {@link org.synyx.urlaubsverwaltung.account.VacationDaysService} it only considers the granted
     * applications and does not depend on today, so the certificate is consistent with the periods it lists.
     */
    private static BigDecimal grantedFromRemaining(Account account, List<Application> grantedApplications,
                                                   DateRange yearRange, WorkingTimeCalendar workingTimeCalendar) {

        final BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        if (grantedApplications.isEmpty() || remainingVacationDays.signum() <= 0) {
            return ZERO;
        }

        final LocalDate expiryDate = account.getExpiryDate();
        final Optional<DateRange> beforeExpiry = expiryDate.isAfter(yearRange.startDate())
            ? Optional.of(new DateRange(yearRange.startDate(), min(List.of(expiryDate.minusDays(1), yearRange.endDate()))))
            : Optional.empty();
        final Optional<DateRange> afterExpiry = expiryDate.isAfter(yearRange.endDate())
            ? Optional.empty()
            : Optional.of(new DateRange(max(List.of(expiryDate, yearRange.startDate())), yearRange.endDate()));

        final BigDecimal remainingVacationDaysNotExpiring = account.doRemainingVacationDaysExpire()
            ? account.getRemainingVacationDaysNotExpiring()
            : remainingVacationDays;

        final VacationDaysLeft left = VacationDaysLeft.builder()
            .withAnnualVacation(account.getActualVacationDays())
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedVacationDaysBeforeExpiry(workingTime(grantedApplications, beforeExpiry, workingTimeCalendar))
            .forUsedVacationDaysAfterExpiry(workingTime(grantedApplications, afterExpiry, workingTimeCalendar))
            .withVacationDaysUsedNextYear(ZERO)
            .build();

        return remainingVacationDays.subtract(left.getRemainingVacationDays());
    }

    private static BigDecimal workingTime(List<Application> applications, Optional<DateRange> dateRange,
                                          WorkingTimeCalendar workingTimeCalendar) {
        return dateRange
            .map(range -> applications.stream()
                .map(application -> workingTimeCalendar.workingTimeInDateRage(application, range))
                .reduce(ZERO, BigDecimal::add))
            .orElse(ZERO);
    }
}
