package org.synyx.urlaubsverwaltung.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Collections.max;
import static java.util.Collections.min;
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

    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final ApplicationService applicationService;
    private final Clock clock;

    @Autowired
    VacationDaysService(WorkingTimeCalendarService workingTimeCalendarService, ApplicationService applicationService, Clock clock) {
        this.workingTimeCalendarService = workingTimeCalendarService;
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
    BigDecimal getTotalLeftVacationDays(Account account) {
        final LocalDate today = LocalDate.now(clock);
        return getVacationDaysLeft(List.of(account), Year.of(account.getYear()))
            .get(account)
            .vacationDaysYear()
            .getLeftVacationDays(today, account.doRemainingVacationDaysExpire(), account.getExpiryDate());
    }

    /**
     * @param holidayAccounts {@link Account} to determine configured expiryDate of {@link Application}s
     * @param year            year to calculate left vacation days for.
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, Year year) {
        return getVacationDaysLeft(holidayAccounts, year, List.of());
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param holidayAccounts         {@link Account} to determine configured expiryDate of {@link Application}s
     * @param year                    year to calculate left vacation days for.
     * @param holidayAccountsNextYear to calculate the vacation days that are already used next year
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, Year year, List<Account> holidayAccountsNextYear) {

        final LocalDate startDate = year.atDay(1);
        final LocalDate endDate = startDate.with(lastDayOfYear());

        return getVacationDaysLeft(holidayAccounts, new DateRange(startDate, endDate), holidayAccountsNextYear);
    }

    /**
     * @param holidayAccounts {@link Account} to determine configured expiryDate of {@link Application}s
     * @param dateRange       date range to calculate left vacation days for. must be within a year.
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, DateRange dateRange) {
        return getVacationDaysLeft(holidayAccounts, dateRange, List.of());
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param holidayAccounts         {@link Account} to determine configured expiryDate of {@link Application}s
     * @param dateRange               date range to calculate left vacation days for. must be within a year.
     * @param holidayAccountsNextYear to calculate the vacation days that are already used next year
     * @return {@link HolidayAccountVacationDays} for every passed {@link Account}. {@link Account}s with no used vacation are included.
     * @throws IllegalArgumentException when dateRange is over one year.
     */
    public Map<Account, HolidayAccountVacationDays> getVacationDaysLeft(List<Account> holidayAccounts, DateRange dateRange, List<Account> holidayAccountsNextYear) {

        final LocalDate from = dateRange.startDate();
        final LocalDate to = dateRange.endDate();

        if (to.getYear() != from.getYear()) {
            throw new IllegalArgumentException(String.format("date range must be in the same year but was from=%s to=%s", from, to));
        }

        final List<Account> holidayAccountsForYear = holidayAccounts.stream().filter(account -> account.getYear() == from.getYear()).toList();
        final List<Person> persons = holidayAccountsForYear.stream().map(Account::getPerson).toList();
        final Map<Person, WorkingTimeCalendar> workingTimeCalendars = workingTimeCalendarService.getWorkingTimesByPersons(persons, Year.of(from.getYear()));
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
     * Calculates the used remaining vacation days based on the given account information
     *
     * @param account to calculate used remaining vacation days of the year of the account
     * @return the used remaining vacation days
     */
    BigDecimal getUsedRemainingVacationDays(Account account) {

        if (account.getRemainingVacationDays().signum() > 0) {

            final Year year = Year.of(account.getYear());
            final VacationDaysLeft left = getVacationDaysLeft(List.of(account), year)
                .get(account)
                .vacationDaysYear();

            final BigDecimal usedVacationDays = account.getActualVacationDays()
                .add(account.getRemainingVacationDays())
                .subtract(left.getVacationDays())
                .subtract(left.getRemainingVacationDays());

            final BigDecimal notUsedVacationDays = usedVacationDays.subtract(account.getActualVacationDays());

            if (notUsedVacationDays.signum() > 0) {
                return notUsedVacationDays;
            }
        }

        return ZERO;
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

    private UsedVacationDaysTuple usedVacationDaysForApplication(
        Account holidayAccount,
        Application application,
        DateRange dateRange,
        WorkingTimeCalendar workingTimeCalendar
    ) {

        final LocalDate holidayAccountValidFrom = holidayAccount.getValidFrom();
        final LocalDate holidayAccountExpiryDate = holidayAccount.getExpiryDate();
        final LocalDate lastDayBeforeExpiryDate = holidayAccountExpiryDate.minusDays(1);

        final LocalDate applicationStartOrFirstDayOfYear = max(List.of(application.getStartDate(), holidayAccountValidFrom.with(firstDayOfYear())));
        final LocalDate applicationEndOrLastDayOfYear = min(List.of(application.getEndDate(), holidayAccountValidFrom.with(lastDayOfYear())));

        final LocalDate applicationStartOrFirstDayOfYearOrFrom = max(List.of(applicationStartOrFirstDayOfYear, dateRange.startDate()));
        final LocalDate applicationEndOrLastDayOfYearOrTo = min(List.of(applicationEndOrLastDayOfYear, dateRange.endDate()));

        // use vacation days scoped to from/to date range
        final BigDecimal dateRangeWorkDaysCountBeforeExpiryDate;
        final BigDecimal dateRangeWorkDaysCountAfterExpiryDate;
        if (applicationStartOrFirstDayOfYearOrFrom.isBefore(holidayAccountExpiryDate)) {
            final LocalDate dateRangeStartAfterExpiryDate = max(List.of(applicationStartOrFirstDayOfYearOrFrom, holidayAccountExpiryDate));
            final LocalDate dateRangeEndBeforeExpiryDate = min(List.of(applicationEndOrLastDayOfYearOrTo, lastDayBeforeExpiryDate));

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
            yearWorkDaysCountBeforeExpiry = workingTimeCalendar.workingTime(applicationStartOrFirstDayOfYear, min(List.of(application.getEndDate(), lastDayBeforeExpiryDate)));
            yearWorkDaysCountAfterExpiry = workingTimeCalendar.workingTime(max(List.of(application.getStartDate(), holidayAccountExpiryDate)), applicationEndOrLastDayOfYear);
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

    private BigDecimal divideBy2(BigDecimal value) {
        return value.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
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
}
