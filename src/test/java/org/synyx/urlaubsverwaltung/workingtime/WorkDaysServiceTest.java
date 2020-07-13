package org.synyx.urlaubsverwaltung.workingtime;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.math.BigDecimal;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link WorkDaysService}.
 */
class WorkDaysServiceTest {

    private WorkDaysService instance;
    private PublicHolidaysService publicHolidaysService;
    private WorkingTimeService workingTimeService;
    private SettingsService settingsService;

    private Application application;
    private WorkingTime workingTime;
    private Person person;

    @BeforeEach
    void setUp() {

        settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(new Settings());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("Holidays_de.xml");
        ManagerParameter managerParameter = ManagerParameters.create(url);
        HolidayManager holidayManager = HolidayManager.getInstance(managerParameter);

        publicHolidaysService = new PublicHolidaysService(settingsService, holidayManager);
        workingTimeService = mock(WorkingTimeService.class);

        instance = new WorkDaysService(publicHolidaysService, workingTimeService, settingsService);

        person = DemoDataCreator.createPerson();
        application = DemoDataCreator.createApplication(person,
            DemoDataCreator.createVacationType(VacationCategory.HOLIDAY));

        workingTime = DemoDataCreator.createWorkingTime();

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person),
            any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));
    }


    /**
     * Test of getWeekDays method, of class OwnCalendarService.
     */
    @Test
    void testGetWeekDays() {

        LocalDate start = LocalDate.of(2011, 11, 16);
        LocalDate end = LocalDate.of(2011, 11, 28);

        double returnValue = instance.getWeekDays(start, end);

        assertNotNull(returnValue);
        assertEquals(9.0, returnValue, 0.0);
    }


    /**
     * Test of getWorkDays method, of class OwnCalendarService.
     */
    @Test
    void testGetVacationDays() {

        // testing for full days
        application.setDayLength(DayLength.FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 10 (considering public holidays and weekends)

        LocalDate start = LocalDate.of(2010, 12, 17);
        LocalDate end = LocalDate.of(2010, 12, 31);

        BigDecimal returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(10.0), returnValue);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 9 (considering public holidays and weekends)

        start = LocalDate.of(2009, 12, 17);
        end = LocalDate.of(2009, 12, 31);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9.0), returnValue);

        // start date and end date are not in the same year
        start = LocalDate.of(2011, 12, 26);
        end = LocalDate.of(2012, 1, 15);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(13.0), returnValue);

        // Labour Day (1st May)
        start = LocalDate.of(2009, 4, 27);
        end = LocalDate.of(2009, 5, 2);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // start date is Sunday, end date Saturday
        start = LocalDate.of(2011, 1, 2);
        end = LocalDate.of(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // testing for half days
        application.setDayLength(DayLength.MORNING);

        start = LocalDate.of(2011, 1, 4);
        end = LocalDate.of(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(1.5), returnValue);
    }


    @Test
    void testGetWorkDaysForTeilzeitPersons() {

        List<Integer> workingDays = Arrays.asList(DayOfWeek.MONDAY.getValue(), DayOfWeek.WEDNESDAY.getValue(),
            DayOfWeek.FRIDAY.getValue(), DayOfWeek.SATURDAY.getValue());
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        LocalDate startDate = LocalDate.of(2013, Month.DECEMBER, 16);
        LocalDate endDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(new BigDecimal("8.0"), returnValue);
    }


    @Test
    void testGetWorkDaysForVollzeitPersons() {

        List<Integer> workingDays = Arrays.asList(DayOfWeek.MONDAY.getValue(), DayOfWeek.TUESDAY.getValue(),
            DayOfWeek.WEDNESDAY.getValue(), DayOfWeek.THURSDAY.getValue(), DayOfWeek.FRIDAY.getValue());
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        LocalDate startDate = LocalDate.of(2013, Month.DECEMBER, 16);
        LocalDate endDate = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(new BigDecimal("9.0"), returnValue);
    }


    @Test
    void testGetWorkDaysHalfDay() {

        // monday
        LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 25);
        LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 25);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, startDate, endDate, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    void testGetWorkDaysZero() {

        // saturday
        LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 23);
        LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 23);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(BigDecimal.ZERO, returnValue.setScale(0));
    }


    @Test
    void testGetWorkDaysHalfDayZero() {

        // saturday
        LocalDate startDate = LocalDate.of(2013, Month.NOVEMBER, 23);
        LocalDate endDate = LocalDate.of(2013, Month.NOVEMBER, 23);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, startDate, endDate, person);

        assertEquals(BigDecimal.ZERO, returnValue.setScale(0));
    }


    @Test
    void testGetWorkDaysForChristmasEve() {

        LocalDate date = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    void testGetWorkDaysForChristmasEveDayLengthMorning() {

        LocalDate date = LocalDate.of(2013, Month.DECEMBER, 24);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    void testGetWorkDaysForNewYearsEve() {

        LocalDate date = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    void testGetWorkDaysForNewYearsEveDayLengthMorning() {

        LocalDate date = LocalDate.of(2013, Month.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHoliday() {

        LocalDate from = LocalDate.of(2013, Month.DECEMBER, 23);
        LocalDate to = LocalDate.of(2014, Month.JANUARY, 2);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("5.0"), returnValue);
    }


    @Test
    void testGetWorkDaysForChristmasEveAndNewYearsHolidayDayLengthMorning() {

        LocalDate from = LocalDate.of(2013, Month.DECEMBER, 23);
        LocalDate to = LocalDate.of(2014, Month.JANUARY, 2);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, from, to, person);

        assertEquals(new BigDecimal("2.5"), returnValue);
    }


    @Test
    void ensureCorrectWorkDaysForAssumptionDayForSystemDefaultFederalState() {

        LocalDate from = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate to = LocalDate.of(2016, Month.AUGUST, 15);

        BigDecimal workDays = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("1.0"), workDays);
    }


    @Test
    void ensureCorrectWorkDaysForAssumptionDayForOverriddenFederalState() {

        LocalDate from = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate to = LocalDate.of(2016, Month.AUGUST, 15);

        workingTime.setFederalStateOverride(FederalState.BAYERN_AUGSBURG);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person),
            any(LocalDate.class)))
            .thenReturn(Optional.of(workingTime));

        BigDecimal workDays = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("0.0"), workDays);
    }
}
