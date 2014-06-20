
package org.synyx.urlaubsverwaltung.core.calendar;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTime;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService}.
 *
 * @author  Aljona Murygina
 */
public class OwnCalendarServiceTest {

    private OwnCalendarService instance;
    private JollydayCalendar jollydayCalendar;
    private WorkingTimeService workingTimeService;
    private Application application;
    private WorkingTime workingTime;
    private Person person;

    @Before
    public void setUp() {

        jollydayCalendar = new JollydayCalendar();
        workingTimeService = Mockito.mock(WorkingTimeService.class);

        instance = new OwnCalendarService(jollydayCalendar, workingTimeService);

        application = new Application();
        person = new Person();
        workingTime = new WorkingTime();

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        Mockito.when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(Mockito.eq(person),
                Mockito.any(DateMidnight.class))).thenReturn(workingTime);
    }


    /**
     * Test of getWeekDays method, of class OwnCalendarService.
     */
    @Test
    public void testGetWorkDays() {

        DateMidnight start = new DateMidnight(2011, 11, 16);
        DateMidnight end = new DateMidnight(2011, 11, 28);

        double returnValue = instance.getWeekDays(start, end);

        assertNotNull(returnValue);
        assertEquals(9.0, returnValue, 0.0);
    }


    /**
     * Test of getWorkDays method, of class OwnCalendarService.
     */
    @Test
    public void testGetVacationDays() {

        // testing for full days
        application.setHowLong(DayLength.FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 10 (considering public holidays and weekends)

        DateMidnight start = new DateMidnight(2010, 12, 17);
        DateMidnight end = new DateMidnight(2010, 12, 31);

        BigDecimal returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(10.0), returnValue);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 9 (considering public holidays and weekends)

        start = new DateMidnight(2009, 12, 17);
        end = new DateMidnight(2009, 12, 31);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9.0), returnValue);

        // start date and end date are not in the same year
        start = new DateMidnight(2011, 12, 26);
        end = new DateMidnight(2012, 1, 15);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(13.0), returnValue);

        // Labour Day (1st May)
        start = new DateMidnight(2009, 4, 27);
        end = new DateMidnight(2009, 5, 2);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // start date is Sunday, end date Saturday
        start = new DateMidnight(2011, 1, 2);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // testing for half days
        application.setHowLong(DayLength.MORNING);

        start = new DateMidnight(2011, 1, 4);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getHowLong(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(1.5), returnValue);
    }


    @Test
    public void testGetPersonalWorkDays() {

        List<Integer> list = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.WEDNESDAY,
                DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(list, DayLength.FULL);

        // 1 week (MON - SUN)
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 25);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 1);

        BigDecimal returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(new BigDecimal("4.0"), returnValue);
    }


    @Test
    public void testGetPersonalWorkDaysOneDay() {

        List<Integer> list = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.WEDNESDAY,
                DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(list, DayLength.FULL);

        // saturday
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 30);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 30);

        BigDecimal returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(BigDecimal.ONE, returnValue.setScale(0));

        // tuesday
        startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 26);
        endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 26);

        returnValue = instance.getPersonalWorkDays(startDate, endDate, workingTime);

        assertEquals(BigDecimal.ZERO, returnValue);
    }


    @Test
    public void testGetWorkDaysForTeilzeitStaff() {

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.WEDNESDAY,
                DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 16);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(new BigDecimal("8.0"), returnValue);
    }


    @Test
    public void testGetWorkDaysForVollzeitStaff() {

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 16);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(new BigDecimal("9.0"), returnValue);
    }


    @Test
    public void testGetWorkDaysHalfDay() {

        // monday
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 25);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 25);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, startDate, endDate, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkDaysZero() {

        // saturday
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 23);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 23);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, startDate, endDate, person);

        assertEquals(BigDecimal.ZERO, returnValue.setScale(0));
    }


    @Test
    public void testGetWorkDaysHalfDayZero() {

        // saturday
        DateMidnight startDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 23);
        DateMidnight endDate = new DateMidnight(2013, DateTimeConstants.NOVEMBER, 23);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, startDate, endDate, person);

        assertEquals(BigDecimal.ZERO, returnValue.setScale(0));
    }


    @Test
    public void testGetWorkDaysForChristmasEve() {

        DateMidnight date = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkDaysForChristmasEveDayLengthMorning() {

        DateMidnight date = new DateMidnight(2013, DateTimeConstants.DECEMBER, 24);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkDaysForNewYearsEve() {

        DateMidnight date = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkDaysForNewYearsEveDayLengthMorning() {

        DateMidnight date = new DateMidnight(2013, DateTimeConstants.DECEMBER, 31);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, date, date, person);

        assertEquals(new BigDecimal("0.5"), returnValue);
    }


    @Test
    public void testGetWorkDaysForChristmasEveAndNewYearsHoliday() {

        DateMidnight from = new DateMidnight(2013, DateTimeConstants.DECEMBER, 23);
        DateMidnight to = new DateMidnight(2014, DateTimeConstants.JANUARY, 2);

        BigDecimal returnValue = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("5.0"), returnValue);
    }


    @Test
    public void testGetWorkDaysForChristmasEveAndNewYearsHolidayDayLengthMorning() {

        DateMidnight from = new DateMidnight(2013, DateTimeConstants.DECEMBER, 23);
        DateMidnight to = new DateMidnight(2014, DateTimeConstants.JANUARY, 2);

        BigDecimal returnValue = instance.getWorkDays(DayLength.MORNING, from, to, person);

        assertEquals(new BigDecimal("2.5"), returnValue);
    }
}
