package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import java.util.Arrays;
import junit.framework.Assert;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;

/**
 * Unit test for {@link CalculationService}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class CalculationServiceTest {

    private CalculationService service;
    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private OwnCalendarService calendarService;

    @Before
    public void setUp() {

        applicationDAO = Mockito.mock(ApplicationDAO.class);
        accountService = Mockito.mock(HolidaysAccountService.class);
        calendarService = new OwnCalendarService();

        service = new CalculationService(applicationDAO, accountService, calendarService);

    }

    /**
     * Test of checkApplication method, of class CalculationService.
     */
    @Test
    public void testCheckApplication() {
        
        
        
    }

    @Test
    public void testGetDaysBeforeApril() {

        Person person = new Person();
        person.setLoginName("horscht");
        
        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.MARCH, 31);

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2011, DateTimeConstants.DECEMBER, 29));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2 before January + 2 after January

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 12));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.MARCH, 16));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a3 = new Application();
        a3.setStartDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 6));
        a3.setEndDate(new DateMidnight(2012, DateTimeConstants.FEBRUARY, 9));
        a3.setHowLong(DayLength.FULL);
        a3.setDays(BigDecimal.valueOf(4));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a2, a3));
        
        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a1));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a4));
        
        BigDecimal days = service.getDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2 + 5 + 4 + 2 = 13
        
        Assert.assertNotNull(days);
        Assert.assertEquals(BigDecimal.valueOf(13).setScale(2), days);
        
    }
    
    @Test
    public void testGetDaysAfterApril() {

        Person person = new Person();
        person.setLoginName("horscht");
        
        DateMidnight firstMilestone = new DateMidnight(2012, DateTimeConstants.APRIL, 1);
        DateMidnight lastMilestone = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        Application a1 = new Application();
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.DECEMBER, 27));
        a1.setEndDate(new DateMidnight(2013, DateTimeConstants.JANUARY, 3));
        a1.setHowLong(DayLength.FULL);
        // must be 4 days at all: 2.5 before January + 2 after January

        Application a2 = new Application();
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 3));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 7));
        a2.setHowLong(DayLength.FULL);
        a2.setDays(BigDecimal.valueOf(5));

        Application a4 = new Application();
        a4.setStartDate(new DateMidnight(2012, DateTimeConstants.MARCH, 29));
        a4.setEndDate(new DateMidnight(2012, DateTimeConstants.APRIL, 5));
        a4.setHowLong(DayLength.FULL);
        // must be 6 days at all: 2 before April + 4 after April

        Mockito.when(applicationDAO.getApplicationsBetweenTwoMilestones(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a2));
        
        Mockito.when(applicationDAO.getApplicationsBeforeFirstMilestone(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a4));

        Mockito.when(applicationDAO.getApplicationsAfterLastMilestone(person, firstMilestone.toDate(), 
                lastMilestone.toDate(), VacationType.HOLIDAY, ApplicationStatus.CANCELLED)).thenReturn(Arrays.asList(a1));
        
        BigDecimal days = service.getDaysBetweenTwoMilestones(person, firstMilestone, lastMilestone);
        // must be: 2.5 + 5 + 4 = 11.5
        
        Assert.assertNotNull(days);
        Assert.assertEquals(BigDecimal.valueOf(11.5).setScale(2), days);
        
    }
}
