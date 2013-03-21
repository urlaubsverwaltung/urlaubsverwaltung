
package org.synyx.urlaubsverwaltung.application.service;

import org.synyx.urlaubsverwaltung.application.service.OverlapService;
import org.synyx.urlaubsverwaltung.application.domain.OverlapCase;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateMidnight;

import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.synyx.urlaubsverwaltung.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 * Unit test for {@link OverlapService}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class OverlapServiceTest {
    
     private OverlapService instance;
     private ApplicationDAO applicationDAO; 
     private Person person;
     
     @Before
     public void setup() {
         
         applicationDAO = Mockito.mock(ApplicationDAO.class);
         instance = new OverlapService(applicationDAO);
         
     }
    
    /** Test of checkOverlap method, of class OverlapService. */
    @Test
    public void testCheckOverlap() {

        // show that right method is used dependant on day length of application

        // full day
        Application app = new Application();
        app.setHowLong(DayLength.FULL);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 30));
        app.setPerson(person);

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getRelevantActiveApplicationsByPeriodForEveryDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person);

        // morning
        app.setHowLong(DayLength.MORNING);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 25));

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person, DayLength.MORNING);

        // noon
        app.setHowLong(DayLength.NOON);
        app.setStartDate(new DateMidnight(2012, 1, 25));
        app.setEndDate(new DateMidnight(2012, 1, 25));

        instance.checkOverlap(app);
        Mockito.verify(applicationDAO).getRelevantActiveApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
            app.getEndDate().toDate(), person, DayLength.NOON);
    }


    /** Test of checkOverlapForFullDay method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForFullDay() {

        // case (1) no overlap at all, with gap
        // a1: 16. - 18. Jan.
        // aNew: 23. - 24. Jan.
        // excepted return value == 1

        Application a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(new ArrayList<Application>());

        OverlapCase returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // case (1) no overlap at all, abuting
        // a1: 16. - 18. Jan.
        // aNew: 19. - 20. Jan.
        // excepted return value == 1

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        // return new and empty list
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(new ArrayList<Application>());

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // case (2) period of aNew is element of the period of a1
        // a1: 16. - 20. Jan.
        // aNew: 17. - 18. Jan.
        // excepted return value == 2

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        List<Application> list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping end of period of a1
        // a1: 16. - 19. Jan.
        // aNew: 18. - 20. Jan.
        // excepted return value == 3

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping start of period of a1
        // aNew: 16. - 19. Jan.
        // a1: 18. - 20. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 20));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // case (3) period of aNew is overlapping two different periods (a1 and a2)
        // aNew: 17. - 26. Jan.
        // a1: 16. - 18. Jan.
        // a2: 25. - 27. Jan.
        // excepted return value == 3

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 26));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        Application a2 = new Application();
        a2.setHowLong(DayLength.FULL);
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 27));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // periods a1 and a2 abut case (1), aNew is element of both and has no gap case (2)
        // aNew: 17. - 23. Jan.
        // a1: 16. - 18. Jan.
        // a2: 19. - 25. Jan.
        // excepted return value == 2

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 17));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));

        a1 = new Application();
        a1.setHowLong(DayLength.FULL);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 16));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 18));

        a2 = new Application();
        a2.setHowLong(DayLength.FULL);
        a2.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 19));
        a2.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));

        list = new ArrayList<Application>();
        list.add(a1);
        list.add(a2);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // there is an existent application for a half day
        // new application for full day overlapping this day
        // because there would be gaps, expected value == 3
        a1 = new Application();
        a1.setHowLong(DayLength.MORNING);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 23));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 25));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.PARTLY_OVERLAPPING, returnValue);

        // there is an existent application for a half day
        // new application for full day on this day
        // expected value == 2
        a1 = new Application();
        a1.setHowLong(DayLength.MORNING);
        a1.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        a1.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        aNew = new Application();
        aNew.setHowLong(DayLength.FULL);
        aNew.setStartDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));
        aNew.setEndDate(new DateMidnight(2012, DateTimeConstants.JANUARY, 24));

        list = new ArrayList<Application>();
        list.add(a1);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodForEveryDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson())).thenReturn(list);

        returnValue = instance.checkOverlap(aNew);

        assertNotNull(returnValue);
        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);
    }


    /** Test of checkOverlapForMorning method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForMorning() {

        OverlapCase returnValue;
        List<Application> list = new ArrayList<Application>();

        // FIRST CHECK: OVERLAP WITH FULL DAY PERIOD

        // there is a holiday from 23.01.2012 to 27.01.2012
        // try to apply for leave for a half day on 25.01.2012
        // list of existent applications for full day contains one entry (Application a)
        // expected return value == 2
        Application a = new Application();
        a.setHowLong(DayLength.FULL);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 27));
        a.setPerson(person);
        list.add(a);

        Application aNew = new Application();
        aNew.setHowLong(DayLength.MORNING);
        aNew.setStartDate(new DateMidnight(2012, 1, 25));
        aNew.setEndDate(new DateMidnight(2012, 1, 25));
        aNew.setPerson(person);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at start of existent application
        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at end of existent application

        aNew.setStartDate(new DateMidnight(2012, 1, 27));
        aNew.setEndDate(new DateMidnight(2012, 1, 27));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application has no overlap with full day period
        // i.e. list of existent applications for full day is empty (and for half days too)
        // expected value == 1
        aNew.setStartDate(new DateMidnight(2012, 1, 28));
        aNew.setEndDate(new DateMidnight(2012, 1, 28));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(
            new ArrayList<Application>()); // no overlap because of different dates

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // SECOND CHECK: OVERLAP WITH HALF DAY

        // existent application for morning i.e. list of existent applications for full day is empty, but list of
        // existent applications for morning is not empty; so there is an overlap with morning application.
        // expected value == 2
        list = new ArrayList<Application>();

        a = new Application();
        a.setHowLong(DayLength.MORNING);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 23));
        a.setPerson(person);
        list.add(a);

        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(list);

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // existent application for noon i.e. there are no existent applications for full day, but for noon (but not for
        // morning!) --> lists with existent applications are empty because there is no overlap! expected value == 1
        a.setHowLong(DayLength.NOON);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.MORNING)).thenReturn(
            new ArrayList<Application>());

        returnValue = instance.checkOverlapForMorning(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);
    }


    /** Test of checkOverlapForNoon method, of class ApplicationServiceImpl. */
    @Test
    public void testCheckOverlapForNoon() {

        OverlapCase returnValue;
        List<Application> list = new ArrayList<Application>();

        // FIRST CHECK: OVERLAP WITH FULL DAY PERIOD

        // there is a holiday from 23.01.2012 to 27.01.2012
        // try to apply for leave for a half day on 25.01.2012
        // list of existent applications for full day contains one entry (Application a)
        // expected return value == 2
        Application a = new Application();
        a.setHowLong(DayLength.FULL);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 27));
        a.setPerson(person);
        list.add(a);

        Application aNew = new Application();
        aNew.setHowLong(DayLength.NOON);
        aNew.setStartDate(new DateMidnight(2012, 1, 25));
        aNew.setEndDate(new DateMidnight(2012, 1, 25));
        aNew.setPerson(person);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at start of existent application
        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application at end of existent application

        aNew.setStartDate(new DateMidnight(2012, 1, 27));
        aNew.setEndDate(new DateMidnight(2012, 1, 27));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // new application has no overlap with full day period
        // i.e. list of existent applications for full day is empty (and for half days empty too)
        // expected value == 1
        aNew.setStartDate(new DateMidnight(2012, 1, 28));
        aNew.setEndDate(new DateMidnight(2012, 1, 28));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>()); // no existent half day applications
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(new ArrayList<Application>()); // no overlap because of different dates

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);

        // SECOND CHECK: OVERLAP WITH HALF DAY

        // existent application for noon i.e. list of existent applications for full day is empty, but list of
        // existent applications for noon is not empty; so there is an overlap with noon application.
        // expected value == 2
        list = new ArrayList<Application>();

        a = new Application();
        a.setHowLong(DayLength.NOON);
        a.setStartDate(new DateMidnight(2012, 1, 23));
        a.setEndDate(new DateMidnight(2012, 1, 23));
        a.setPerson(person);
        list.add(a);

        aNew.setStartDate(new DateMidnight(2012, 1, 23));
        aNew.setEndDate(new DateMidnight(2012, 1, 23));

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(list);

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.FULLY_OVERLAPPING, returnValue);

        // existent application for noon i.e. there are no existent applications for full day, but for noon --> lists
        // with existent applications are empty because there is no overlap!
        // expected value == 1
        a.setHowLong(DayLength.MORNING);

        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.FULL)).thenReturn(new ArrayList<Application>());
        Mockito.when(applicationDAO.getRelevantActiveApplicationsByPeriodAndDayLength(aNew.getStartDate().toDate(),
                aNew.getEndDate().toDate(), aNew.getPerson(), DayLength.NOON)).thenReturn(new ArrayList<Application>());

        returnValue = instance.checkOverlapForNoon(aNew);

        assertEquals(OverlapCase.NO_OVERLAPPING, returnValue);
    }
    
}
