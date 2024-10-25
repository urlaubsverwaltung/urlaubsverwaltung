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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

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
    void getWorkDaysWithMultipleWorkingTimesOverOneAbsence() {

        when(settingsService.getSettings()).thenReturn(new Settings());

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
    void getWorkDaysWithHalfDayMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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

        when(settingsService.getSettings()).thenReturn(new Settings());

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


    private HolidayManager getHolidayManager() {
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.GERMANY));
    }

    public static WorkingTime createWorkingTime(Person person, LocalDate validFrom, DayOfWeek... daysOfWeek) {
        final WorkingTime workingTime = new WorkingTime(person, validFrom, GERMANY_BADEN_WUERTTEMBERG, false);
        workingTime.setWorkingDays(List.of(daysOfWeek), FULL);
        return workingTime;
    }

}
