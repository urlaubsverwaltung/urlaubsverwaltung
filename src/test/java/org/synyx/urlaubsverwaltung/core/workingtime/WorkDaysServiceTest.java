
package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.io.IOException;
import java.math.BigDecimal;
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
 *
 * @author  Aljona Murygina
 */
public class WorkDaysServiceTest {

    private WorkDaysService instance;
    private PublicHolidaysService publicHolidaysService;
    private WorkingTimeService workingTimeService;
    private SettingsService settingsService;

    private Application application;
    private WorkingTime workingTime;
    private Person person;

    @Before
    public void setUp() {

        settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(new Settings());

        publicHolidaysService = new PublicHolidaysService(settingsService);
        workingTimeService = mock(WorkingTimeService.class);

        instance = new WorkDaysService(publicHolidaysService, workingTimeService, settingsService);

        person = TestDataCreator.createPerson();
        application = TestDataCreator.createApplication(person,
                TestDataCreator.createVacationType(VacationCategory.HOLIDAY));

        workingTime = TestDataCreator.createWorkingTime();

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person),
                    any(DateMidnight.class)))
            .thenReturn(Optional.of(workingTime));
    }


    /**
     * Test of getWeekDays method, of class OwnCalendarService.
     */
    @Test
    public void testGetWeekDays() {

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
        application.setDayLength(DayLength.FULL);

        // Testing for 2010: 17.12. is Friday, 31.12. is Friday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 10 (considering public holidays and weekends)

        DateMidnight start = new DateMidnight(2010, 12, 17);
        DateMidnight end = new DateMidnight(2010, 12, 31);

        BigDecimal returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(10.0), returnValue);

        // Testing for 2009: 17.12. is Thursday, 31.12. ist Thursday
        // between these dates: 2 public holidays (25., 26.) plus 2*0.5 public holidays (24., 31.)
        // total days: 14
        // netto days: 9 (considering public holidays and weekends)

        start = new DateMidnight(2009, 12, 17);
        end = new DateMidnight(2009, 12, 31);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(9.0), returnValue);

        // start date and end date are not in the same year
        start = new DateMidnight(2011, 12, 26);
        end = new DateMidnight(2012, 1, 15);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(13.0), returnValue);

        // Labour Day (1st May)
        start = new DateMidnight(2009, 4, 27);
        end = new DateMidnight(2009, 5, 2);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // start date is Sunday, end date Saturday
        start = new DateMidnight(2011, 1, 2);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(4.0), returnValue);

        // testing for half days
        application.setDayLength(DayLength.MORNING);

        start = new DateMidnight(2011, 1, 4);
        end = new DateMidnight(2011, 1, 8);

        returnValue = instance.getWorkDays(application.getDayLength(), start, end, person);

        assertNotNull(returnValue);
        assertEquals(BigDecimal.valueOf(1.5), returnValue);
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


    @Test
    public void ensureCorrectWorkDaysForAssumptionDayForSystemDefaultFederalState() {

        DateMidnight from = new DateMidnight(2016, DateTimeConstants.AUGUST, 15);
        DateMidnight to = new DateMidnight(2016, DateTimeConstants.AUGUST, 15);

        BigDecimal workDays = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("1.0"), workDays);
    }


    @Test
    public void ensureCorrectWorkDaysForAssumptionDayForOverriddenFederalState() {

        DateMidnight from = new DateMidnight(2016, DateTimeConstants.AUGUST, 15);
        DateMidnight to = new DateMidnight(2016, DateTimeConstants.AUGUST, 15);

        workingTime.setFederalStateOverride(FederalState.BAYERN_AUGSBURG);

        when(workingTimeService.getByPersonAndValidityDateEqualsOrMinorDate(eq(person),
                    any(DateMidnight.class)))
            .thenReturn(Optional.of(workingTime));

        BigDecimal workDays = instance.getWorkDays(DayLength.FULL, from, to, person);

        assertEquals(new BigDecimal("0.0"), workDays);
    }
}
