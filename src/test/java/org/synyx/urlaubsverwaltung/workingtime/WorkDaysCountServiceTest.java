package org.synyx.urlaubsverwaltung.workingtime;

import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysServiceImpl;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static java.math.BigDecimal.TEN;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN;

@ExtendWith(MockitoExtension.class)
class WorkDaysCountServiceTest {

    private WorkDaysCountService sut;

    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        final var publicHolidaysService = new PublicHolidaysServiceImpl(settingsService, Map.of("de", getHolidayManager()));
        sut = new WorkDaysCountService(publicHolidaysService, workingTimeService);
    }

    @Test
    void getWorkDaysWithPublicHolidaysAndBothOnWeekend() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2010, 12, 17);
        final LocalDate endDate = LocalDate.of(2010, 12, 31);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates:
        //   Public Holidays : 2 (25., 26.) and 2*0.5 (24., 31.) => 3
        //   Weekends : 2 (18-19 and 25-26 - already public holidays)) => 2
        // total days: 15
        // netto days: 10 (considering public holidays and weekends)
        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(TEN);
    }

    @Test
    void getWorkDaysWithPublicHolidaysAndOneOnWeekend() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2009, 12, 17);
        final LocalDate endDate = LocalDate.of(2009, 12, 31);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates:
        //   Public Holidays : 2 (25., 26.) and 2*0.5 (24., 31.) => 3
        //   Weekends : 2 (19-20 and 26-27 - one is already public holidays)) => 3
        // total days: 15
        // netto days: 9 (considering public holidays and weekends)
        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(9));
    }

    @Test
    void getWorkDaySkipsDayNotCoveredByAnyWorkingTime() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        // Mon Jan 8 to Fri Jan 12, 2024
        final LocalDate startDate = LocalDate.of(2024, 1, 8);
        final LocalDate endDate = LocalDate.of(2024, 1, 12);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        // Wed Jan 10 intentionally not covered by any DateRange
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class)))
            .thenReturn(Map.of(
                new DateRange(startDate, LocalDate.of(2024, 1, 9)), workingTime,
                new DateRange(LocalDate.of(2024, 1, 11), endDate), workingTime
            ));

        // Mon, Tue, (Wed skipped — no WorkingTime), Thu, Fri = 4
        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void getWorkDaysWithMultipleWorkingTimesOverOneAbsence() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2022, 1, 10);
        final LocalDate midDate = LocalDate.of(2022, 1, 17);
        final LocalDate endDate = LocalDate.of(2022, 1, 23);

        final WorkingTime workingTimeFullWeek = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        final WorkingTime workingTimeHalfWeek = createWorkingTime(person, midDate, MONDAY, TUESDAY, WEDNESDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class)))
            .thenReturn(Map.of(
                new DateRange(startDate, midDate.minusDays(1)), workingTimeFullWeek,
                new DateRange(midDate, endDate), workingTimeHalfWeek
            ));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(8));
    }

    @Test
    void getWorkDaysWithMultipleWorkingTimesAndDifferentFederalStates() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        // Period: Dec 20, 2023 (Wednesday) - Jan 5, 2024 (Friday)
        final LocalDate startDate = LocalDate.of(2023, 12, 20);
        final LocalDate midDate = LocalDate.of(2024, 1, 1);
        final LocalDate endDate = LocalDate.of(2024, 1, 5);

        // First period: Baden-Wuerttemberg (Dec 20, 2023 - Dec 31, 2023)
        final WorkingTime workingTimeBW = new WorkingTime(person, startDate, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTimeBW.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);

        // Second period: Bayern (Jan 1, 2024 - Jan 5, 2024)
        final WorkingTime workingTimeBY = new WorkingTime(person, midDate, GERMANY_BAYERN, false);
        workingTimeBY.setWorkingDays(List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY), FULL);

        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class)))
            .thenReturn(Map.of(
                new DateRange(startDate, midDate.minusDays(1)), workingTimeBW,
                new DateRange(midDate, endDate), workingTimeBY
            ));

        // BW period (Dec 20-31, 2023):
        //   Wed 20: workday (1)
        //   Thu 21: workday (1)
        //   Fri 22: workday (1)
        //   Sat 23: weekend, skipped
        //   Sun 24: weekend, skipped (Christmas Eve falls on Sunday)
        //   Mon 25: Christmas 1st day - public holiday => 0 workdays
        //   Tue 26: Christmas 2nd day - public holiday => 0 workdays
        //   Wed 27: workday (1)
        //   Thu 28: workday (1)
        //   Fri 29: workday (1)
        //   Sat 30: weekend, skipped
        //   Sun 31: weekend, skipped (New Years Eve falls on Sunday)
        //   BW subtotal: 6
        //
        // BY period (Jan 1-5, 2024):
        //   Mon 1: New Year - full public holiday => 0 workdays
        //   Tue 2: workday (1)
        //   Wed 3: workday (1)
        //   Thu 4: workday (1)
        //   Fri 5: workday (1)
        //   BY subtotal: 4
        //
        // Total: 6 + 4 = 10

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void getWorkDaysWithHalfDayMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2011, 1, 4);
        final LocalDate endDate = LocalDate.of(2011, 1, 4);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkDaysWithHalfDaysMorning() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2011, 1, 4);
        final LocalDate endDate = LocalDate.of(2011, 1, 8);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    void getWorkDaysWithHalfDayNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2011, 1, 4);
        final LocalDate endDate = LocalDate.of(2011, 1, 4);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(NOON, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkDaysWithHalfDaysNoon() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2011, 1, 4);
        final LocalDate endDate = LocalDate.of(2011, 1, 8);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));


        final BigDecimal workDaysCount = sut.getWorkDaysCount(NOON, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    void getWorkDaysWithSundayToSaturday() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        // start date is Sunday, end date Saturday
        final LocalDate startDate = LocalDate.of(2011, 1, 2);
        final LocalDate endDate = LocalDate.of(2011, 1, 8);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void getWorkDaysWithPublicHolidaysOverLabourDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        // Labour Day (1st May)
        final LocalDate startDate = LocalDate.of(2009, 4, 27);
        final LocalDate endDate = LocalDate.of(2009, 5, 2);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void getWorkDaysWithPublicHolidaysOverYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        // start date and end date are not in the same year
        final LocalDate startDate = LocalDate.of(2011, 12, 26);
        final LocalDate endDate = LocalDate.of(2012, 1, 15);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(13));
    }

    @Test
    void testGetWorkDaysForTeilzeitPersons() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 16);
        final LocalDate endDate = LocalDate.of(2013, DECEMBER, 31);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, WEDNESDAY, FRIDAY, SATURDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(8));
    }

    @Test
    void testGetWorkDaysForVollzeitPersons() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 16);
        final LocalDate endDate = LocalDate.of(2013, DECEMBER, 31);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(9));
    }

    @Test
    void testGetWorkDaysHalfDay() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        // monday
        final LocalDate startDate = LocalDate.of(2013, NOVEMBER, 25);
        final LocalDate endDate = LocalDate.of(2013, NOVEMBER, 25);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysZero() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        // saturday
        final LocalDate startDate = LocalDate.of(2013, NOVEMBER, 23);
        final LocalDate endDate = LocalDate.of(2013, NOVEMBER, 23);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetWorkDaysHalfDayZero() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        // Saturday
        final LocalDate date = LocalDate.of(2013, NOVEMBER, 23);

        final WorkingTime workingTime = createWorkingTime(person, date, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetWorkDaysForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2013, DECEMBER, 24);

        final WorkingTime workingTime = createWorkingTime(person, date, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForChristmasEveDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2013, DECEMBER, 24);

        final WorkingTime workingTime = createWorkingTime(person, date, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2013, DECEMBER, 31);

        final WorkingTime workingTime = createWorkingTime(person, date, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForNewYearsEveDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate date = LocalDate.of(2013, DECEMBER, 31);

        final WorkingTime workingTime = createWorkingTime(person, date, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(date, date), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHoliday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate endDate = LocalDate.of(2014, JANUARY, 2);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHolidayDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate endDate = LocalDate.of(2014, JANUARY, 2);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }


    @Test
    void getWorkDaysCountByYearForPeriodWithinOneYear() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2010, DECEMBER, 17);
        final LocalDate endDate = LocalDate.of(2010, DECEMBER, 31);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final SortedMap<Integer, BigDecimal> workDaysByYear = sut.getWorkDaysCountByYear(FULL, startDate, endDate, person);
        assertThat(workDaysByYear).containsExactly(entry(2010, BigDecimal.valueOf(10.0)));
    }

    @Test
    void getWorkDaysCountByYearForPeriodSpanningTwoYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate endDate = LocalDate.of(2014, JANUARY, 2);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        // 2013: 23.12. (Mon) = 1, 24.12. = 0.5, 25./26.12. public holidays, 27.12. (Fri) = 1,
        //       28./29.12. weekend, 30.12. (Mon) = 1, 31.12. = 0.5 => 4
        // 2014: 01.01. public holiday, 02.01. (Thu) = 1 => 1
        final SortedMap<Integer, BigDecimal> workDaysByYear = sut.getWorkDaysCountByYear(FULL, startDate, endDate, person);
        assertThat(workDaysByYear).containsExactly(entry(2013, BigDecimal.valueOf(4.0)), entry(2014, BigDecimal.valueOf(1.0)));
    }

    @Test
    void getWorkDaysCountByYearForPeriodSpanningTwoYearsDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate endDate = LocalDate.of(2014, JANUARY, 2);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        final SortedMap<Integer, BigDecimal> workDaysByYear = sut.getWorkDaysCountByYear(MORNING, startDate, endDate, person);
        assertThat(workDaysByYear).containsExactly(entry(2013, BigDecimal.valueOf(2.0)), entry(2014, BigDecimal.valueOf(0.5)));
    }

    @Test
    void getWorkDaysCountByYearContainsYearWithoutWorkDays() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final LocalDate startDate = LocalDate.of(2021, DECEMBER, 25);
        final LocalDate endDate = LocalDate.of(2022, JANUARY, 1);

        final WorkingTime workingTime = createWorkingTime(person, startDate, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY);
        when(workingTimeService.getWorkingTimesByPersonAndDateRange(eq(person), any(DateRange.class))).thenReturn(Map.of(new DateRange(startDate, endDate), workingTime));

        // 2021: 25./26.12. weekend, 27.12.-30.12. (Mon-Thu) = 4, 31.12. = 0.5 => 4.5
        // 2022: 01.01. public holiday on saturday => 0
        final SortedMap<Integer, BigDecimal> workDaysByYear = sut.getWorkDaysCountByYear(FULL, startDate, endDate, person);
        assertThat(workDaysByYear).containsExactly(entry(2021, BigDecimal.valueOf(4.5)), entry(2022, BigDecimal.valueOf(0.0)));
    }

    private HolidayManager getHolidayManager() {
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
    }

    public static WorkingTime createWorkingTime(Person person, LocalDate validFrom, DayOfWeek... daysOfWeek) {
        final WorkingTime workingTime = new WorkingTime(person, validFrom, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(daysOfWeek), FULL);
        return workingTime;
    }

}
