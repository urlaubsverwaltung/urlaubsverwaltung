package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
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
     * @param holidayAccounts      {@link Account} to determine configured expiryDate of {@link Application}s
     * @param workingTimeCalendars {@link WorkingTimeCalendar} to calculate the used vacation days for the {@link Account}s persons.
     * @param year                 year to calculate left vacation days for.
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts,
                                                                        Map<Person, WorkingTimeCalendar> workingTimeCalendars,
                                                                        Year year) {
        return getVacationDaysLeft(holidayAccounts, workingTimeCalendars, year, List.of());
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param holidayAccounts         {@link Account} to determine configured expiryDate of {@link Application}s
     * @param workingTimeCalendars    {@link WorkingTimeCalendar} to calculate the used vacation days for the {@link Account}s persons.
     * @param year                    year to calculate left vacation days for.
     * @param holidayAccountsNextYear to calculate the vacation days that are already used next year
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts,
                                                                        Map<Person, WorkingTimeCalendar> workingTimeCalendars,
                                                                        Year year, List<Account> holidayAccountsNextYear) {
        final LocalDate startDate = year.atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());

        return getVacationDaysLeft(holidayAccounts, workingTimeCalendars, new DateRange(startDate, endDate), holidayAccountsNextYear);
    }

    /**
     * @param holidayAccounts      {@link Account} to determine configured expiryDate of {@link Application}s
     * @param workingTimeCalendars {@link WorkingTimeCalendar} to calculate the used vacation days for the {@link Account}s persons.
     * @param dateRange            date range to calculate left vacation days for. must be within a year.
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, Map<Person, WorkingTimeCalendar> workingTimeCalendars, DateRange dateRange) {
        return getVacationDaysLeft(holidayAccounts, workingTimeCalendars, dateRange, List.of());
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param holidayAccounts         {@link Account} to determine configured expiryDate of {@link Application}s
     * @param workingTimeCalendars    {@link WorkingTimeCalendar} to calculate the used vacation days for the {@link Account}s persons.
     * @param dateRange               date range to calculate left vacation days for. must be within a year.
     * @param holidayAccountsNextYear to calculate the vacation days that are already used next year
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, Map<Person, WorkingTimeCalendar> workingTimeCalendars,
                                                                        DateRange dateRange, List<Account> holidayAccountsNextYear) {

        final LocalDate from = dateRange.startDate();
        final LocalDate to = dateRange.endDate();

        if (to.getYear() != from.getYear()) {
            throw new IllegalArgumentException(String.format("date range must be in the same year but was from=%s to=%s", from, to));
        }

        final List<Account> holidayAccountsForYear = holidayAccounts.stream().filter(account -> account.getYear() == from.getYear()).toList();
        return getUsedVacationDays(holidayAccountsForYear, dateRange, workingTimeCalendars).entrySet().stream()
            .map(entry -> {
                final Account account = entry.getKey();
                final BigDecimal vacationDays = account.getActualVacationDays();
                final BigDecimal remainingVacationDays = account.getRemainingVacationDays();
                final BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

                final UsedVacationDaysTuple usedVacationDaysTuple = entry.getValue();
                final UsedVacationDaysYear usedVacationDaysYear = usedVacationDaysTuple.usedVacationDaysYear();

                final BigDecimal vacationDaysUsedNextYear = holidayAccountsNextYear.stream()
                    .filter(holidayAccountNextYear -> holidayAccountNextYear.getPerson().equals(account.getPerson()))
                    .filter(holidayAccountNextYear -> holidayAccountNextYear.getYear() == from.getYear() + 1)
                    .findFirst()
                    .map(this::getUsedRemainingVacationDays)
                    .orElse(ZERO);

                final VacationDaysLeft vacationDaysLeftYear = VacationDaysLeft.builder()
                    .withAnnualVacation(vacationDays)
                    .withRemainingVacation(remainingVacationDays)
                    .notExpiring(remainingVacationDaysNotExpiring)
                    .forUsedVacationDaysBeforeExpiry(usedVacationDaysYear.usedVacationDaysBeforeExpiryDate())
                    .forUsedVacationDaysAfterExpiry(usedVacationDaysYear.usedVacationDaysAfterExpiryDate())
                    .withVacationDaysUsedNextYear(vacationDaysUsedNextYear)
                    .build();

                final UsedVacationDaysDateRange usedVacationDaysDateRange = usedVacationDaysTuple.usedVacationDaysDateRange();
                final VacationDaysLeft vacationDaysLeftDateRange = VacationDaysLeft.builder()
                    .withAnnualVacation(vacationDays)
                    .withRemainingVacation(remainingVacationDays)
                    .notExpiring(remainingVacationDaysNotExpiring)
                    .forUsedVacationDaysBeforeExpiry(usedVacationDaysDateRange.usedVacationDaysBeforeExpiryDate())
                    .forUsedVacationDaysAfterExpiry(usedVacationDaysDateRange.usedVacationDaysAfterExpiryDate())
                    .withVacationDaysUsedNextYear(vacationDaysUsedNextYear)
                    .build();

                return new HolidayAccountVacationDays(account, vacationDaysLeftYear, vacationDaysLeftDateRange);
            })
            .collect(toMap(HolidayAccountVacationDays::account, identity()));
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
    BigDecimal getTotalLeftVacationDays(Account account) {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate startDate = Year.of(account.getYear()).atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());
        return getVacationDaysLeft(startDate, endDate, account)
            .getLeftVacationDays(today, account.doRemainingVacationDaysExpire(), account.getExpiryDate());
    }

    /**
     * Calculates the used remaining vacation days based on the given account information
     *
     * @param account to calculate used remaining vacation days of the year of the account
     * @return the used remaining vacation days
     */
    public BigDecimal getUsedRemainingVacationDays(Account account) {
        final LocalDate firstDayOfYear = Year.of(account.getYear()).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        return getUsedRemainingVacationDays(firstDayOfYear, lastDayOfYear, account);
    }

    BigDecimal getUsedVacationDaysBetweenTwoMilestones(Person person, LocalDate firstMilestone, LocalDate lastMilestone) {

        if (firstMilestone.isAfter(lastMilestone)) {
            return ZERO;
        }

        return applicationService.getApplicationsForACertainPeriodAndPersonAndVacationCategory(firstMilestone, lastMilestone, person, activeStatuses(), HOLIDAY).stream()
            .map(application -> getUsedVacationDays(application, person, firstMilestone, lastMilestone))
            .reduce(ZERO, BigDecimal::add);
    }

    private Map<Account, UsedVacationDaysTuple> getUsedVacationDays(List<Account> holidayAccounts, DateRange dateRange, Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson) {

        final LocalDate firstDayOfYear = dateRange.startDate().with(firstDayOfYear());
        final LocalDate lastDayOfYear = dateRange.endDate().with(lastDayOfYear());

        final List<Person> persons = holidayAccounts.stream().map(Account::getPerson).distinct().toList();
        final List<Application> applicationsTouchingDateRange = applicationService.getForStatesAndPerson(activeStatuses(), persons, firstDayOfYear, lastDayOfYear);

        return getUsedVacationDaysBetweenTwoMilestones(holidayAccounts, applicationsTouchingDateRange, dateRange, workingTimeCalendarsByPerson);
    }

    private Map<Account, UsedVacationDaysTuple> getUsedVacationDaysBetweenTwoMilestones(List<Account> holidayAccounts, List<Application> applications, DateRange dateRange, Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson) {

        final Map<Person, List<Application>> applicationsByPerson = applications.stream()
            .filter(application -> application.getVacationType().getCategory().equals(HOLIDAY))
            .collect(groupingBy(Application::getPerson));

        // check persons actual working time for the applicationForLeave.
        // the returned working time is the duration of usedVacationDays.
        // e.g. working time: MONDAY=FULL TUESDAY=FULL WEDNESDAY=ZERO THURSDAY=FULL FRIDAY=FULL
        //      application for a full week -> 4 used vacation days
        return holidayAccounts.stream().flatMap(holidayAccount -> {
            final Person person = holidayAccount.getPerson();

            if (applicationsByPerson.containsKey(person)) {
                final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarsByPerson.get(person);
                return applicationsByPerson.get(person).stream()
                    .map(application -> usedVacationDaysForApplication(holidayAccount, application, dateRange, workingTimeCalendar))
                    .map(usedVacationDays -> Map.entry(holidayAccount, usedVacationDays));
            }

            // person has no applied applicationForLeaves -> zero used vacation days
            return Stream.of(Map.entry(holidayAccount, UsedVacationDaysTuple.identity()));
        }).collect(groupingBy(
            // group by account
            Entry::getKey,
            // and summarize used vacation days of the account's applications
            reducing(UsedVacationDaysTuple.identity(), Entry::getValue, Addable::add)
        ));
    }

    private UsedVacationDaysTuple usedVacationDaysForApplication(Account holidayAccount,
                                                                 Application application,
                                                                 DateRange dateRange,
                                                                 WorkingTimeCalendar workingTimeCalendar) {

        final LocalDate holidayAccountValidFrom = holidayAccount.getValidFrom();
        final LocalDate holidayAccountExpiryDate = holidayAccount.getExpiryDate();
        final LocalDate lastDayBeforeExpiryDate = holidayAccountExpiryDate.minusDays(1);

        final LocalDate applicationStartOrFirstDayOfYear = max(application.getStartDate(), holidayAccountValidFrom.with(firstDayOfYear()));
        final LocalDate applicationEndOrLastDayOfYear = min(application.getEndDate(), holidayAccountValidFrom.with(lastDayOfYear()));

        final LocalDate applicationStartOrFirstDayOfYearOrFrom = max(applicationStartOrFirstDayOfYear, dateRange.startDate());
        final LocalDate applicationEndOrLastDayOfYearOrTo = min(applicationEndOrLastDayOfYear, dateRange.endDate());

        // use vacation days scoped to from/to date range
        final BigDecimal dateRangeWorkDaysCountBeforeExpiryDate;
        final BigDecimal dateRangeWorkDaysCountAfterExpiryDate;
        if (applicationStartOrFirstDayOfYearOrFrom.isBefore(holidayAccountExpiryDate)) {
            final LocalDate dateRangeStartAfterExpiryDate = max(applicationStartOrFirstDayOfYearOrFrom, holidayAccountExpiryDate);
            final LocalDate dateRangeEndBeforeExpiryDate = min(applicationEndOrLastDayOfYearOrTo, lastDayBeforeExpiryDate);

            dateRangeWorkDaysCountBeforeExpiryDate = workingTimeCalendar.workingTime(applicationStartOrFirstDayOfYearOrFrom, dateRangeEndBeforeExpiryDate);
            dateRangeWorkDaysCountAfterExpiryDate = workingTimeCalendar.workingTime(dateRangeStartAfterExpiryDate, applicationEndOrLastDayOfYearOrTo);
        } else {
            dateRangeWorkDaysCountBeforeExpiryDate = ZERO;
            dateRangeWorkDaysCountAfterExpiryDate = workingTimeCalendar.workingTime(applicationStartOrFirstDayOfYearOrFrom, applicationEndOrLastDayOfYearOrTo);
        }

        final WorkingTimeCalendar.WorkingDayInformation workingDayInformation = workingTimeCalendar.workingDays().get(application.getStartDate());

        final UsedVacationDaysDateRange dateRangeUsedVacationDays;
        if (application.getDayLength().isHalfDay() && workingDayInformation != null && !workingDayInformation.hasHalfDayPublicHoliday()) {
            // halfDay application is only possible for one localDate.
            // so we can safely divide the calculated workDays by 2.
            dateRangeUsedVacationDays = new UsedVacationDaysDateRange(divideBy2(dateRangeWorkDaysCountBeforeExpiryDate), divideBy2(dateRangeWorkDaysCountAfterExpiryDate));
        } else {
            dateRangeUsedVacationDays = new UsedVacationDaysDateRange(dateRangeWorkDaysCountBeforeExpiryDate, dateRangeWorkDaysCountAfterExpiryDate);
        }

        // use vacation days considering full year
        final BigDecimal yearWorkDaysCountBeforeExpiry;
        final BigDecimal yearWorkDaysCountAfterExpiry;
        if (applicationStartOrFirstDayOfYear.isBefore(holidayAccountExpiryDate)) {
            yearWorkDaysCountBeforeExpiry = workingTimeCalendar.workingTime(applicationStartOrFirstDayOfYear, min(application.getEndDate(), lastDayBeforeExpiryDate));
            yearWorkDaysCountAfterExpiry = workingTimeCalendar.workingTime(max(application.getStartDate(), holidayAccountExpiryDate), applicationEndOrLastDayOfYear);
        } else {
            yearWorkDaysCountBeforeExpiry = ZERO;
            yearWorkDaysCountAfterExpiry = workingTimeCalendar.workingTime(applicationStartOrFirstDayOfYear, applicationEndOrLastDayOfYear);
        }

        final UsedVacationDaysYear yearUsedVacationDays;
        if (application.getDayLength().isHalfDay() && workingDayInformation != null && !workingDayInformation.hasHalfDayPublicHoliday()) {
            // halfDay application is only possible for one localDate.
            // so we can safely divide the calculated workDays by 2.
            yearUsedVacationDays = new UsedVacationDaysYear(divideBy2(yearWorkDaysCountBeforeExpiry), divideBy2(yearWorkDaysCountAfterExpiry));
        } else {
            yearUsedVacationDays = new UsedVacationDaysYear(yearWorkDaysCountBeforeExpiry, yearWorkDaysCountAfterExpiry);
        }

        return new UsedVacationDaysTuple(dateRangeUsedVacationDays, yearUsedVacationDays);
    }

    private VacationDaysLeft getVacationDaysLeft(LocalDate start, LocalDate end, Account account) {
        return getVacationDaysLeft(start, end, account, Optional.empty());
    }

    private BigDecimal divideBy2(BigDecimal value) {
        return value.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
    }

    private static LocalDate max(LocalDate localDate, LocalDate localDate2) {
        return localDate.isBefore(localDate2) ? localDate2 : localDate;
    }

    private static LocalDate min(LocalDate localDate, LocalDate localDate2) {
        return localDate.isBefore(localDate2) ? localDate : localDate2;
    }

    private interface Addable<T> {
        T add(T toAdd);
    }

    private record UsedVacationDaysTuple(UsedVacationDaysDateRange usedVacationDaysDateRange,
                                         UsedVacationDaysYear usedVacationDaysYear) implements Addable<UsedVacationDaysTuple> {

        @Override
        public UsedVacationDaysTuple add(UsedVacationDaysTuple toAdd) {
            return new UsedVacationDaysTuple(
                usedVacationDaysDateRange.add(toAdd.usedVacationDaysDateRange),
                usedVacationDaysYear.add(toAdd.usedVacationDaysYear)
            );
        }

        static UsedVacationDaysTuple identity() {
            return new UsedVacationDaysTuple(new UsedVacationDaysDateRange(ZERO, ZERO), new UsedVacationDaysYear(ZERO, ZERO));
        }
    }

    private record UsedVacationDaysYear(BigDecimal usedVacationDaysBeforeExpiryDate,
                                        BigDecimal usedVacationDaysAfterExpiryDate) implements Addable<UsedVacationDaysYear> {

        @Override
        public UsedVacationDaysYear add(UsedVacationDaysYear toAdd) {
            return new UsedVacationDaysYear(
                usedVacationDaysBeforeExpiryDate.add(toAdd.usedVacationDaysBeforeExpiryDate),
                usedVacationDaysAfterExpiryDate.add(toAdd.usedVacationDaysAfterExpiryDate)
            );
        }
    }

    private record UsedVacationDaysDateRange(BigDecimal usedVacationDaysBeforeExpiryDate,
                                             BigDecimal usedVacationDaysAfterExpiryDate) implements Addable<UsedVacationDaysDateRange> {

        @Override
        public UsedVacationDaysDateRange add(UsedVacationDaysDateRange toAdd) {
            return new UsedVacationDaysDateRange(
                usedVacationDaysBeforeExpiryDate.add(toAdd.usedVacationDaysBeforeExpiryDate),
                usedVacationDaysAfterExpiryDate.add(toAdd.usedVacationDaysAfterExpiryDate)
            );
        }
    }

    private VacationDaysLeft getVacationDaysLeft(LocalDate start, LocalDate end, Account account, Optional<Account> nextYear) {

        final BigDecimal vacationDays = account.getActualVacationDays();
        final BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        final BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        final BigDecimal usedVacationDaysBeforeExpiryDate;
        final BigDecimal usedVacationDaysAfterExpiryDate;

        if (account.doRemainingVacationDaysExpire()) {
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

        final BigDecimal usedVacationDaysNextYear = nextYear
            .map(this::getUsedRemainingVacationDays)
            .orElse(ZERO);

        return VacationDaysLeft.builder()
            .withAnnualVacation(vacationDays)
            .withRemainingVacation(remainingVacationDays)
            .notExpiring(remainingVacationDaysNotExpiring)
            .forUsedVacationDaysBeforeExpiry(usedVacationDaysBeforeExpiryDate)
            .forUsedVacationDaysAfterExpiry(usedVacationDaysAfterExpiryDate)
            .withVacationDaysUsedNextYear(usedVacationDaysNextYear)
            .build();
    }

    private BigDecimal getUsedRemainingVacationDays(LocalDate start, LocalDate end, Account account) {

        if (start.isAfter(end)) {
            return ZERO;
        }

        if (account.getRemainingVacationDays().signum() > 0) {

            final VacationDaysLeft left = getVacationDaysLeft(start, end, account);

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

    private BigDecimal getUsedVacationDays(Application application, Person person, LocalDate firstMilestone, LocalDate lastMilestone) {
        final LocalDate startDate = application.getStartDate().isBefore(firstMilestone) ? firstMilestone : application.getStartDate();
        final LocalDate endDate = application.getEndDate().isAfter(lastMilestone) ? lastMilestone : application.getEndDate();
        return workDaysCountService.getWorkDaysCount(application.getDayLength(), startDate, endDate, person);
    }
}
