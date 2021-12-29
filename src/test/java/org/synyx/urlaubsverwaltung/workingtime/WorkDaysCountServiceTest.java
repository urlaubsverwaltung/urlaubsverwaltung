package org.synyx.urlaubsverwaltung.workingtime;

import de.jollyday.HolidayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static de.jollyday.ManagerParameters.create;
import static java.math.BigDecimal.TEN;
import static java.time.Month.DECEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createWorkingTime;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.MORNING;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;

@ExtendWith(MockitoExtension.class)
class WorkDaysCountServiceTest {

    private WorkDaysCountService sut;

    @Mock
    private WorkingTimeService workingTimeService;
    @Mock
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        final var publicHolidaysService = new PublicHolidaysService(settingsService, getHolidayManager());
        sut = new WorkDaysCountService(publicHolidaysService, workingTimeService);
    }

    @Test
    void testGetWeekDays() {

        final LocalDate start = LocalDate.of(2011, 11, 16);
        final LocalDate end = LocalDate.of(2011, 11, 28);

        final double weekDaysCount = sut.getWeekDaysCount(start, end);
        assertThat(weekDaysCount).isEqualTo(9.0);
    }

    @Test
    void getWorkDaysWithPublicHolidaysAndBothOnWeekend() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for full days
        application.setDayLength(FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates:
        //   Public Holidays : 2 (25., 26.) and 2*0.5 (24., 31.) => 3
        //   Weekends : 2 (18-19 and 25-26 - already public holidays)) => 2
        // total days: 15
        // netto days: 10 (considering public holidays and weekends)
        final LocalDate start = LocalDate.of(2010, 12, 17);
        final LocalDate end = LocalDate.of(2010, 12, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(TEN);
    }

    @Test
    void getWorkDaysWithPublicHolidaysAndOneOnWeekend() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for full days
        application.setDayLength(FULL);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates:
        //   Public Holidays : 2 (25., 26.) and 2*0.5 (24., 31.) => 3
        //   Weekends : 2 (19-20 and 26-27 - one is already public holidays)) => 3
        // total days: 15
        // netto days: 9 (considering public holidays and weekends)
        final LocalDate start = LocalDate.of(2009, 12, 17);
        final LocalDate end = LocalDate.of(2009, 12, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(9));
    }

    @Test
    void getWorkDaysWithHalfDayMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for half days morning
        application.setDayLength(MORNING);

        final LocalDate start = LocalDate.of(2011, 1, 4);
        final LocalDate end = LocalDate.of(2011, 1, 4);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkDaysWithHalfDaysMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for half days morning
        application.setDayLength(MORNING);

        final LocalDate start = LocalDate.of(2011, 1, 4);
        final LocalDate end = LocalDate.of(2011, 1, 8);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    void getWorkDaysWithHalfDayNoon() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person();
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for half days noon
        application.setDayLength(NOON);

        final LocalDate start = LocalDate.of(2011, 1, 4);
        final LocalDate end = LocalDate.of(2011, 1, 4);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkDaysWithHalfDaysNoon() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for half days noon
        application.setDayLength(NOON);

        final LocalDate start = LocalDate.of(2011, 1, 4);
        final LocalDate end = LocalDate.of(2011, 1, 8);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    void getWorkDaysWithSundayToSaturday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for full days
        application.setDayLength(FULL);

        // start date is Sunday, end date Saturday
        final LocalDate start = LocalDate.of(2011, 1, 2);
        final LocalDate end = LocalDate.of(2011, 1, 8);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void getWorkDaysWithPublicHolidaysOverLabourDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for full days
        application.setDayLength(FULL);

        // Labour Day (1st May)
        final LocalDate start = LocalDate.of(2009, 4, 27);
        final LocalDate end = LocalDate.of(2009, 5, 2);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(4));
    }

    @Test
    void getWorkDaysWithPublicHolidaysOverYears() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, TestDataCreator.createVacationTypeEntity(HOLIDAY));

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // testing for full days
        application.setDayLength(FULL);

        // start date and end date are not in the same year
        final LocalDate start = LocalDate.of(2011, 12, 26);
        final LocalDate end = LocalDate.of(2012, 1, 15);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(application.getDayLength(), start, end, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(13));
    }

    @Test
    void testGetWorkDaysForTeilzeitPersons() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        List<DayOfWeek> workingDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 16);
        final LocalDate endDate = LocalDate.of(2013, DECEMBER, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(8));
    }

    @Test
    void testGetWorkDaysForVollzeitPersons() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        List<DayOfWeek> workingDays = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        workingTime.setWorkingDays(workingDays, FULL);

        final LocalDate startDate = LocalDate.of(2013, DECEMBER, 16);
        final LocalDate endDate = LocalDate.of(2013, DECEMBER, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(9));
    }

    @Test
    void testGetWorkDaysHalfDay() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // monday
        final LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 25);
        final LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 25);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysZero() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // saturday
        final LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 23);
        final LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 23);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetWorkDaysHalfDayZero() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        // saturday
        final LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 23);
        final LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 23);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, startDate, endDate, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testGetWorkDaysForChristmasEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate date = LocalDate.of(2013, DECEMBER, 24);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForChristmasEveDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate date = LocalDate.of(2013, DECEMBER, 24);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForNewYearsEve() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate date = LocalDate.of(2013, DECEMBER, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForNewYearsEveDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate date = LocalDate.of(2013, DECEMBER, 31);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, date, date, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHoliday() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate from = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate to = LocalDate.of(2014, Month.JANUARY, 2);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(FULL, from, to, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHolidayDayLengthMorning() {

        when(settingsService.getSettings()).thenReturn(new Settings());

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final WorkingTime workingTime = createWorkingTime();
        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person), any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        final LocalDate from = LocalDate.of(2013, DECEMBER, 23);
        final LocalDate to = LocalDate.of(2014, Month.JANUARY, 2);

        final BigDecimal workDaysCount = sut.getWorkDaysCount(MORNING, from, to, person);
        assertThat(workDaysCount).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }

    private HolidayManager getHolidayManager() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource("Holidays_de.xml");
        return HolidayManager.getInstance(create(url));
    }
}
