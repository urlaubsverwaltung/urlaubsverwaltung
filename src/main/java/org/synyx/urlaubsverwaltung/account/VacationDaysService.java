package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;


/**
 * Provides calculation of used / left vacation days.
 */
@Service
public class VacationDaysService {

    private final WorkDaysCountService workDaysCountService;
    private final ApplicationService applicationService;
    private final Clock clock;

    @Autowired
    public VacationDaysService(WorkDaysCountService workDaysCountService, ApplicationService applicationService, Clock clock) {
        this.workDaysCountService = workDaysCountService;
        this.applicationService = applicationService;
        this.clock = clock;
    }

    /**
     * Calculates the total number of days that are left to be used for applying for leave.
     *
     * <p>NOTE: The calculation depends on the current date. If it's before the expiry date, the left remaining vacation days are
     * relevant for calculation and if it's after the expiry date, only the not expiring remaining vacation days are relevant for
     * calculation.</p>
     *
     * @param account {@link Account}
     * @return total number of left vacation days
     */
    public BigDecimal calculateTotalLeftVacationDays(Account account) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate firstDayOfYear = Year.of(account.getYear()).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        return calculateTotalLeftVacationDays(firstDayOfYear, lastDayOfYear, today, account);
    }

    public BigDecimal calculateTotalLeftVacationDays(LocalDate start, LocalDate end, LocalDate today, Account account) {
        return getVacationDaysLeft(start, end, account, Optional.empty())
            .getLeftVacationDays(today, account.doRemainigVacationDaysExpire(), account.getExpiryDate());
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param account  the account for the year to calculate the vacation days for
     * @param nextYear the account for following year, if available
     * @return information about the vacation days left for that year
     */
    public VacationDaysLeft getVacationDaysLeft(Account account, Optional<Account> nextYear) {
        final LocalDate firstDayOfYear = Year.of(account.getYear()).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        return getVacationDaysLeft(firstDayOfYear, lastDayOfYear, account, nextYear);
    }

    public VacationDaysLeft getVacationDaysLeft(LocalDate start, LocalDate end, Account account, Optional<Account> nextYear) {

        final BigDecimal vacationDays = account.getActualVacationDays();
        final BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        final BigDecimal usedVacationDaysBeforeExpiryDate;
        final BigDecimal usedVacationDaysAfterExpiryDate;

        if (account.doRemainigVacationDaysExpire()) {
            final LocalDate lastDayBeforeExpiryDate = account.getExpiryDate().minusDays(1);
            final LocalDate endBeforeExpiryDate = end.isAfter(lastDayBeforeExpiryDate) ? lastDayBeforeExpiryDate : end;

            final LocalDate expiryDate = account.getExpiryDate();
            final LocalDate startAfterExpiryDate = start.isBefore(expiryDate) ? expiryDate : start;

            usedVacationDaysBeforeExpiryDate = getUsedVacationDaysBetweenTwoMilestones(account.getPerson(), start, endBeforeExpiryDate);
            usedVacationDaysAfterExpiryDate = getUsedVacationDaysBetweenTwoMilestones(account.getPerson(), startAfterExpiryDate, end);
        } else {
            usedVacationDaysBeforeExpiryDate = getUsedVacationDaysBetweenTwoMilestones(account.getPerson(), start, end);
            usedVacationDaysAfterExpiryDate = ZERO;
        }

        final BigDecimal usedVacationDaysNextYear = nextYear.map(this::getUsedRemainingVacationDays).orElse(ZERO);

        return VacationDaysLeft.builder()
            .withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedVacationDaysBeforeExpiry(usedVacationDaysBeforeExpiryDate)
            .forUsedVacationDaysAfterExpiry(usedVacationDaysAfterExpiryDate)
            .withVacationDaysUsedNextYear(usedVacationDaysNextYear)
            .build();
    }

    public BigDecimal getUsedRemainingVacationDays(Account account) {

        if (account.getRemainingVacationDays().signum() > 0) {

            final LocalDate firstDayOfYear = Year.of(account.getYear()).atDay(1);
            final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
            final VacationDaysLeft left = getVacationDaysLeft(firstDayOfYear, lastDayOfYear, account, Optional.empty());

            final BigDecimal totalUsed = account.getActualVacationDays()
                .add(account.getRemainingVacationDays())
                .subtract(left.getVacationDays())
                .subtract(left.getRemainingVacationDays());

            final BigDecimal remainingUsed = totalUsed.subtract(account.getActualVacationDays());

            if (remainingUsed.signum() > 0) {
                return remainingUsed;
            }
        }
        return ZERO;
    }

    BigDecimal getUsedVacationDaysBetweenTwoMilestones(Person person, LocalDate firstMilestone, LocalDate lastMilestone) {

        if (firstMilestone.isAfter(lastMilestone)) {
            return ZERO;
        }

        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        return applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(firstMilestone, lastMilestone, person, statuses, HOLIDAY).stream()
            .map(application -> getUsedVacationDays(application, person, firstMilestone, lastMilestone))
            .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal getUsedVacationDays(Application application, Person person, LocalDate firstMilestone, LocalDate lastMilestone) {
        final LocalDate startDate = application.getStartDate().isBefore(firstMilestone) ? firstMilestone : application.getStartDate();
        final LocalDate endDate = application.getEndDate().isAfter(lastMilestone) ? lastMilestone : application.getEndDate();
        return workDaysCountService.getWorkDaysCount(application.getDayLength(), startDate, endDate, person);
    }
}
