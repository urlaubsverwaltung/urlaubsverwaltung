/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.Urlaubsanspruch;
import org.synyx.urlaubsverwaltung.domain.Urlaubskonto;

import java.util.List;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.UrlaubsanspruchDAO;
import org.synyx.urlaubsverwaltung.dao.UrlaubskontoDAO;

/**
 * @author  aljona
 */
public class KontoServiceImplTest {

    private UrlaubskontoDAO urlaubskontoDAO;
    private UrlaubsanspruchDAO urlaubsanspruchDAO;
    private OwnCalendarService calendarService;
    private KontoService kontoService;

    public KontoServiceImplTest() {
        urlaubskontoDAO = Mockito.mock(UrlaubskontoDAO.class);
        urlaubsanspruchDAO = Mockito.mock(UrlaubsanspruchDAO.class);
        calendarService = Mockito.mock(OwnCalendarService.class);
        kontoService = new KontoServiceImpl(urlaubskontoDAO, urlaubsanspruchDAO, calendarService);
    }

    /** Test of newUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testNewUrlaubsanspruch() {
        Mockito.when(urlaubsanspruchDAO.save((Urlaubsanspruch) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Urlaubsanspruch catchedObject = (Urlaubsanspruch) (args[0]);
                assertEquals("petar", catchedObject.getPerson().getFirstName());
                assertEquals(new Double(30.0), catchedObject.getVacationDays());
                assertEquals(new Integer(2000), catchedObject.getYear());

                return null;
            }
        });

        Person person = new Person();
        person.setFirstName("petar");

        kontoService.newUrlaubsanspruch(person, 2000, 30.0);
    }

    /** Test of saveUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testSaveUrlaubsanspruch() {
        Mockito.when(urlaubsanspruchDAO.save((Urlaubsanspruch) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Urlaubsanspruch catchedObject = (Urlaubsanspruch) (args[0]);
                assertEquals("petar", catchedObject.getPerson().getFirstName());
                assertEquals(new Double(30.0), catchedObject.getVacationDays());
                assertEquals(new Integer(2000), catchedObject.getYear());

                return null;
            }
        });

        Person person = new Person();
        person.setFirstName("petar");

        Urlaubsanspruch anspruch = new Urlaubsanspruch();
        anspruch.setPerson(person);
        anspruch.setVacationDays(30.0);
        anspruch.setYear(2000);

        kontoService.saveUrlaubsanspruch(anspruch);
    }

    /** Test of saveUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testSaveUrlaubskonto() {
        Mockito.when(urlaubskontoDAO.save((Urlaubskonto) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Urlaubskonto catchedObject = (Urlaubskonto) (args[0]);
                assertEquals("petar", catchedObject.getPerson().getFirstName());
                assertEquals(new Double(30.0), catchedObject.getVacationDays());
                assertEquals(new Double(5.0), catchedObject.getRestVacationDays());
                assertEquals(new Integer(2000), catchedObject.getYear());

                return null;
            }
        });

        Person person = new Person();
        person.setFirstName("petar");

        Urlaubskonto konto = new Urlaubskonto();
        konto.setPerson(person);
        konto.setVacationDays(30.0);
        konto.setYear(2000);
        konto.setRestVacationDays(5.0);

        kontoService.saveUrlaubskonto(konto);
    }

    /** Test of newUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testNewUrlaubskonto() {
        Mockito.when(urlaubskontoDAO.save((Urlaubskonto) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Urlaubskonto catchedObject = (Urlaubskonto) (args[0]);
                assertEquals("petar", catchedObject.getPerson().getFirstName());
                assertEquals(new Double(30.0), catchedObject.getVacationDays());
                assertEquals(new Double(5.0), catchedObject.getRestVacationDays());
                assertEquals(new Integer(2000), catchedObject.getYear());

                return null;
            }
        });

        Person person = new Person();
        person.setFirstName("petar");

        kontoService.newUrlaubskonto(person, 30.0, 5.0, 2000);
    }

    /** Test of getUrlaubsanspruch method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubsanspruch() {
        Mockito.when(urlaubsanspruchDAO.getUrlaubsanspruchByDate(Mockito.anyInt(), (Person) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Integer year = (Integer) (args[0]);
                Person person = (Person) (args[1]);
                assertEquals("petar", person.getFirstName());
                assertEquals(new Integer(2000), year);

                return null;
            }
        });



        Person person = new Person();
        person.setFirstName("petar");

        kontoService.getUrlaubsanspruch(2000, person);
    }

    /** Test of getUrlaubskonto method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubskonto() {
        Mockito.when(urlaubskontoDAO.getUrlaubskontoForDateAndPerson(Mockito.anyInt(), (Person) (Mockito.any()))).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Integer year = (Integer) (args[0]);
                Person person = (Person) (args[1]);
                assertEquals("petar", person.getFirstName());
                assertEquals(new Integer(2000), year);

                return null;
            }
        });

        Person person = new Person();
        person.setFirstName("petar");

        kontoService.getUrlaubskonto(2000, person);
    }

    /** Test of getUrlaubskontoForYear method, of class KontoServiceImpl. */
    @Test
    public void testGetUrlaubskontoForYear() {
        Mockito.when(urlaubskontoDAO.getUrlaubskontoForYear(Mockito.anyInt())).thenAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {

                Object[] args = invocation.getArguments();
                Integer year = (Integer) (args[0]);
                assertEquals(new Integer(2000), year);

                return null;
            }
        });

        kontoService.getUrlaubskontoForYear(2000);
    }

    /** Test of rollbackUrlaub method, of class KontoServiceImpl. */
    @Test
    public void testRollbackUrlaub() {

        KontoService spy = Mockito.spy(kontoService);
        Urlaubsanspruch urlaubsanspruch = new Urlaubsanspruch();
        urlaubsanspruch.setVacationDays(20.0);

        Antrag antrag = new Antrag();
        antrag.setStartDate(new DateMidnight(2000, 12, 20));
        antrag.setEndDate(new DateMidnight(2001, 1, 10));
        
        //Mockito.when(spy.getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()))).thenReturn(urlaubsanspruch);
        Mockito.doReturn(urlaubsanspruch).when(spy).getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()));
        Mockito.doNothing().when(spy).rollbackNoticeJanuary((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()), (Urlaubskonto) (Mockito.any()), Mockito.anyDouble(), Mockito.anyDouble());

        spy.rollbackUrlaub(antrag);

        //ks should call rollbacknoticejanuary
        Mockito.verify(spy).rollbackNoticeJanuary((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()), (Urlaubskonto) (Mockito.any()), Mockito.anyDouble(), Mockito.anyDouble());
        
        antrag.setStartDate(new DateMidnight(2001, 3, 20));
        antrag.setEndDate(new DateMidnight(2001, 3, 22));
        
        //Mockito.when(spy.getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()))).thenReturn(urlaubsanspruch);
        Mockito.doReturn(urlaubsanspruch).when(spy).getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()));
        Mockito.doNothing().when(spy).rollbackNoticeApril((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()), Mockito.anyDouble());
        spy.rollbackUrlaub(antrag);
        

        //ks should call rollbacknoticeapril
        Mockito.verify(spy).rollbackNoticeApril((Antrag) (Mockito.any()), (Urlaubskonto) (Mockito.any()), Mockito.anyDouble());
    }

    /** Test of rollbackNoticeJanuary method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeJanuary() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       Urlaubskonto kontoNextYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(5.0);
       Mockito.when(kontoNextYear.getVacationDays()).thenReturn(15.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(kontoNextYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 12, 20));
       antrag.setEndDate(new DateMidnight(2001, 1, 10));
       
       kontoService.rollbackNoticeJanuary(antrag, kontoThisYear, kontoNextYear, 30.0, 30.0);
       
       Mockito.verify(kontoThisYear).setVacationDays(10.0);
       Mockito.verify(kontoNextYear).setVacationDays(20.0);
    }
    
    /** Test of rollbackNoticeApril method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeAprilAfter() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(22.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 6, 20));
       antrag.setEndDate( new DateMidnight(2001, 6, 30));
       
       kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
       
       Mockito.verify(kontoThisYear).setVacationDays(27.0);
    }

    /** Test of rollbackNoticeApril method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeAprilBefore() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(28.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 3, 20));
       antrag.setEndDate(new DateMidnight(2001, 3, 30));
       
       kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
       
       Mockito.verify(kontoThisYear).setVacationDays(30.0);
       Mockito.verify(kontoThisYear).setRestVacationDays(3.0);
    }
    
        /** Test of rollbackNoticeApril method, of class KontoServiceImpl. */
    @Test
    public void testRollbackNoticeAprilOva() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(22.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(1.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 3, 20));
       antrag.setEndDate(new DateMidnight(2001, 4, 10));
       
       kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
       
       Mockito.verify(kontoThisYear).setVacationDays(30.0);
       Mockito.verify(kontoThisYear).setRestVacationDays(3.0);
    }

    /** Test of noticeJanuary method, of class KontoServiceImpl. */
    @Test
    public void testNoticeJanuary() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       Urlaubskonto kontoNextYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(5.0);
       Mockito.when(kontoNextYear.getVacationDays()).thenReturn(15.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(kontoNextYear.getRestVacationDays()).thenReturn(0.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 12, 20));
       antrag.setEndDate(new DateMidnight(2001, 1, 10));
       
       kontoService.noticeJanuary(antrag, kontoThisYear, kontoNextYear);
       
       Mockito.verify(kontoThisYear).setVacationDays(0.0);
       Mockito.verify(kontoNextYear).setVacationDays(10.0);
    }

    /** Test of noticeApril method, of class KontoServiceImpl. */
    @Test
    public void testNoticeAprilBefore() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(2.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 3, 20));
       antrag.setEndDate(new DateMidnight(2001, 3, 30));
       
       kontoService.noticeApril(antrag, kontoThisYear);
       
       Mockito.verify(kontoThisYear).setVacationDays(27.0);
       Mockito.verify(kontoThisYear).setRestVacationDays(0.0);
    }
    
    /** Test of noticeApril method, of class KontoServiceImpl. */
    @Test
    public void testNoticeAprilAfter() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(2.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate(new DateMidnight(2000, 6, 20));
       antrag.setEndDate(new DateMidnight(2001, 6, 30));
       
       kontoService.noticeApril(antrag, kontoThisYear);
       
       Mockito.verify(kontoThisYear).setVacationDays(25.0);
    }
    
    /** Test of noticeApril method, of class KontoServiceImpl. */
    @Test
    public void testNoticeAprilOva() {

       Urlaubskonto kontoThisYear = Mockito.mock(Urlaubskonto.class);
       
       Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
       Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(7.0);
       Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
       
       Antrag antrag = new Antrag();
       antrag.setStartDate( new DateMidnight(2000, 3, 20));
       antrag.setEndDate( new DateMidnight(2001, 4, 30));
       
       kontoService.noticeApril(antrag, kontoThisYear);
       
       Mockito.verify(kontoThisYear).setVacationDays(25.0);
       Mockito.verify(kontoThisYear).setRestVacationDays(2.0);
    }
}
