package org.synyx.urlaubsverwaltung.absence.statistics;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar.WorkingDayInformation.WorkingTimeCalendarEntryType.PUBLIC_HOLIDAY;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.fullWorkday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.noWorkday;
import static org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarFactory.workingTimeCalendarMondayToFriday;

class MonthlyAbsenceDaysTest {

    private static final StaticMessageSource MESSAGE_SOURCE = new StaticMessageSource();

    private final MonthlyAbsenceDays sut = new MonthlyAbsenceDays();

    private static Person person(long id) {
        final Person person = new Person();
        person.setId(id);
        return person;
    }

    private static VacationType<?> vacationType(long id) {
        return TestDataCreator.createVacationType(id, HOLIDAY, MESSAGE_SOURCE);
    }

    private static Application application(Person person, VacationType<?> vacationType, DayLength dayLength,
                                            LocalDate startDate, LocalDate endDate) {
        final Application application = new Application();
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setDayLength(dayLength);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setStatus(WAITING);
        return application;
    }

    private static WorkingTimeCalendar calendarWithOverride(LocalDate from, LocalDate to, LocalDate overriddenDate,
                                                             WorkingDayInformation overriddenDay) {
        final Map<LocalDate, WorkingDayInformation> days = new HashMap<>();
        for (LocalDate date : new DateRange(from, to)) {
            days.put(date, date.equals(overriddenDate) ? overriddenDay : fullWorkday());
        }
        return new WorkingTimeCalendar(days);
    }

    @Test
    void applicationWithinOneMonthIsCountedInThatMonthOnly() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);

        // monday 2024-03-04 to friday 2024-03-08 -> 5 working days
        final Application application = application(person, vacationType, FULL,
            LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8));

        final WorkingTimeCalendar calendar = workingTimeCalendarMondayToFriday(year.atDay(1), year.atDay(year.length()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        final List<BigDecimal> daysByMonth = actual.get(vacationType).daysByMonth();
        assertThat(daysByMonth.get(2)).isEqualByComparingTo("5"); // march, index 2
        assertThat(actual.get(vacationType).yearSum()).isEqualByComparingTo("5");
        for (int month = 0; month < 12; month++) {
            if (month != 2) {
                assertThat(daysByMonth.get(month)).isEqualByComparingTo(ZERO);
            }
        }
    }

    @Test
    void applicationAcrossMonthBoundaryIsSplitBetweenBothMonths() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);

        // monday 2024-01-29 to friday 2024-02-02 -> jan 29,30,31 + feb 1,2
        final Application application = application(person, vacationType, FULL,
            LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 2));

        final WorkingTimeCalendar calendar = workingTimeCalendarMondayToFriday(year.atDay(1), year.atDay(year.length()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        final List<BigDecimal> daysByMonth = actual.get(vacationType).daysByMonth();
        assertThat(daysByMonth.get(0)).isEqualByComparingTo("3"); // january
        assertThat(daysByMonth.get(1)).isEqualByComparingTo("2"); // february
        assertThat(actual.get(vacationType).yearSum()).isEqualByComparingTo("5");
    }

    @Test
    void applicationAcrossYearBoundaryOnlyCountsThePortionOfTheRequestedYear() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);

        // monday 2024-12-30 to friday 2025-01-03 -> dec 30,31 + jan 1,2,3
        final Application application = application(person, vacationType, FULL,
            LocalDate.of(2024, 12, 30), LocalDate.of(2025, 1, 3));

        final WorkingTimeCalendar calendar = workingTimeCalendarMondayToFriday(
            LocalDate.of(2024, 12, 1), LocalDate.of(2025, 1, 31));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actualPastYear =
            sut.calculate(Year.of(2024), List.of(application), Map.of(person, calendar));
        assertThat(actualPastYear.get(vacationType).daysByMonth().get(11)).isEqualByComparingTo("2"); // december
        assertThat(actualPastYear.get(vacationType).yearSum()).isEqualByComparingTo("2");

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actualNextYear =
            sut.calculate(Year.of(2025), List.of(application), Map.of(person, calendar));
        assertThat(actualNextYear.get(vacationType).daysByMonth().get(0)).isEqualByComparingTo("3"); // january
        assertThat(actualNextYear.get(vacationType).yearSum()).isEqualByComparingTo("3");
    }

    @Test
    void halfDayApplicationCountsAsHalfADay() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);
        final LocalDate date = LocalDate.of(2024, 3, 4);

        final Application application = application(person, vacationType, MORNING, date, date);

        final WorkingTimeCalendar calendar = new WorkingTimeCalendar(Map.of(date, fullWorkday()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        assertThat(actual.get(vacationType).daysByMonth().get(2)).isEqualByComparingTo("0.5");
        assertThat(actual.get(vacationType).yearSum()).isEqualByComparingTo("0.5");
    }

    @Test
    void nonWorkingWeekdayOfAPartTimePersonDoesNotCount() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);

        // monday 2024-03-04 to friday 2024-03-08, wednesday is this person's free weekday
        final Application application = application(person, vacationType, FULL,
            LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8));

        final LocalDate freeWeekday = LocalDate.of(2024, 3, 6);
        final WorkingTimeCalendar calendar = calendarWithOverride(
            LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31), freeWeekday, noWorkday());

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        assertThat(actual.get(vacationType).daysByMonth().get(2)).isEqualByComparingTo("4");
    }

    @Test
    void publicHolidayWithinTheApplicationPeriodDoesNotCount() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);

        // monday 2024-03-04 to friday 2024-03-08, wednesday is a public holiday
        final Application application = application(person, vacationType, FULL,
            LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8));

        final LocalDate holiday = LocalDate.of(2024, 3, 6);
        final WorkingDayInformation publicHoliday = new WorkingDayInformation(DayLength.ZERO, PUBLIC_HOLIDAY, PUBLIC_HOLIDAY);
        final WorkingTimeCalendar calendar = calendarWithOverride(
            LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31), holiday, publicHoliday);

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        assertThat(actual.get(vacationType).daysByMonth().get(2)).isEqualByComparingTo("4");
    }

    @Test
    void waitingApplicationCounts() {

        final Person person = person(1L);
        final VacationType<?> vacationType = vacationType(1000L);
        final Year year = Year.of(2024);
        final LocalDate date = LocalDate.of(2024, 3, 4);

        final Application application = application(person, vacationType, FULL, date, date);
        assertThat(application.getStatus()).isEqualTo(WAITING);

        final WorkingTimeCalendar calendar = new WorkingTimeCalendar(Map.of(date, fullWorkday()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        assertThat(actual.get(vacationType).daysByMonth().get(2)).isEqualByComparingTo("1");
    }

    @Test
    void multipleVacationTypesInTheSameMonthAreReportedSeparately() {

        final Person person = person(1L);
        final VacationType<?> vacationTypeA = vacationType(1000L);
        final VacationType<?> vacationTypeB = vacationType(2000L);
        final Year year = Year.of(2024);

        final Application applicationA = application(person, vacationTypeA, FULL,
            LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 4));
        final Application applicationB = application(person, vacationTypeB, FULL,
            LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 5));

        final WorkingTimeCalendar calendar = workingTimeCalendarMondayToFriday(year.atDay(1), year.atDay(year.length()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(applicationA, applicationB), Map.of(person, calendar));

        assertThat(actual.get(vacationTypeA).daysByMonth().get(2)).isEqualByComparingTo("1");
        assertThat(actual.get(vacationTypeB).daysByMonth().get(2)).isEqualByComparingTo("1");
    }

    @Test
    void vacationTypeWithoutAnyDayIsAbsentFromTheResult() {

        final Person person = person(1L);
        final VacationType<?> vacationTypeWithoutDays = vacationType(1000L);
        final Year year = Year.of(2024);
        final LocalDate saturday = LocalDate.of(2024, 3, 9);

        // saturday, not a working day -> contributes zero days
        final Application application = application(person, vacationTypeWithoutDays, FULL, saturday, saturday);

        final WorkingTimeCalendar calendar = workingTimeCalendarMondayToFriday(year.atDay(1), year.atDay(year.length()));

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(year, List.of(application), Map.of(person, calendar));

        assertThat(actual).isEmpty();
    }

    @Test
    void emptyApplicationListYieldsAnEmptyResultWithoutException() {

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> actual =
            sut.calculate(Year.of(2024), List.of(), Map.of());

        assertThat(actual).isEmpty();
    }
}
