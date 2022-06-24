package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.util.DateUtil.isBeforeApril;


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
     * <p>NOTE: The calculation depends on the current date. If it's before April, the left remaining vacation days are
     * relevant for calculation and if it's after April, only the not expiring remaining vacation days are relevant for
     * calculation.</p>
     *
     * @param account {@link Account}
     * @return total number of left vacation days
     */
    public BigDecimal calculateTotalLeftVacationDays(Account account) {

        final VacationDaysLeft vacationDaysLeft = getVacationDaysLeft(account, Optional.empty());

        // it's before April - the left remaining vacation days must be used
        final LocalDate now = LocalDate.now(clock);
        if (now.getYear() == account.getYear() && isBeforeApril(now, account.getYear())) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            // it's after April - only the left not expiring remaining vacation days must be used
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
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

        final BigDecimal vacationDays = account.getActualVacationDays();
        final BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        final BigDecimal usedVacationDaysBeforeApril = getUsedVacationDaysBeforeApril(account);
        final BigDecimal usedVacationDaysAfterApril = getUsedVacationDaysAfterApril(account);
        final BigDecimal usedVacationDaysNextYear = getUsedRemainingVacationDays(nextYear);

        return VacationDaysLeft.builder()
            .withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedDaysBeforeApril(usedVacationDaysBeforeApril)
            .forUsedDaysAfterApril(usedVacationDaysAfterApril)
            .withVacationDaysUsedNextYear(usedVacationDaysNextYear)
            .build();
    }

    /**
     * Returns the already used vacations from last year of the given account.
     *
     * @param account the account for the year to calculate the vacation days that are used from the year before
     * @return total number of used vacations
     */
    public BigDecimal getUsedRemainingVacationDays(Optional<Account> account) {
        if (account.isPresent() && account.get().getRemainingVacationDays().signum() > 0) {

            final VacationDaysLeft left = getVacationDaysLeft(account.get(), Optional.empty());

            final BigDecimal totalUsed = account.get().getActualVacationDays()
                .add(account.get().getRemainingVacationDays())
                .subtract(left.getVacationDays())
                .subtract(left.getRemainingVacationDays());

            final BigDecimal remainingUsed = totalUsed.subtract(account.get().getActualVacationDays());

            if (remainingUsed.signum() > 0) {
                return remainingUsed;
            }
        }
        return ZERO;
    }

    BigDecimal getUsedVacationDaysBeforeApril(Account account) {
        final LocalDate firstOfJanuary = YearMonth.of(account.getYear(), JANUARY).atDay(1);
        final LocalDate lastOfMarch = YearMonth.of(account.getYear(), MARCH).atEndOfMonth();
        return getUsedVacationDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
    }

    BigDecimal getUsedVacationDaysAfterApril(Account account) {
        final LocalDate firstOfApril = YearMonth.of(account.getYear(), APRIL).atDay(1);
        final LocalDate lastOfDecember = YearMonth.of(account.getYear(), DECEMBER).atEndOfMonth();
        return getUsedVacationDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastOfDecember);
    }

    BigDecimal getUsedVacationDaysBetweenTwoMilestones(Person person, LocalDate firstMilestone, LocalDate lastMilestone) {

        // get all applications for leave for a person
        final List<Application> allApplicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(firstMilestone, lastMilestone, person);

        // TODO We need application.hasStatus(TEMPORARY_ALLOWED)
        // filter them since only WAITING, ALLOWED and ALLOWED_CANCELLATION_REQUESTED applications for leave of type holiday are relevant
        final List<Application> applicationsForLeave = allApplicationsForLeave.stream()
            .filter(application -> HOLIDAY.equals(application.getVacationType().getCategory()))
            .filter(application -> application.hasStatus(WAITING) || application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED))
            .collect(toList());

        BigDecimal usedDays = ZERO;
        for (Application applicationForLeave : applicationsForLeave) {
            LocalDate startDate = applicationForLeave.getStartDate();
            LocalDate endDate = applicationForLeave.getEndDate();

            if (startDate.isBefore(firstMilestone)) {
                startDate = firstMilestone;
            }

            if (endDate.isAfter(lastMilestone)) {
                endDate = lastMilestone;
            }

            usedDays = usedDays.add(workDaysCountService.getWorkDaysCount(applicationForLeave.getDayLength(), startDate, endDate, person));
        }

        return usedDays;
    }
}
