/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.mockito.Mockito;

import org.mockito.invocation.InvocationOnMock;

import org.mockito.stubbing.Answer;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.HolidayEntitlementDAO;
import org.synyx.urlaubsverwaltung.dao.HolidaysAccountDAO;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  aljona
 */
public class HolidaysAccountServiceImplTest {

    private HolidaysAccountDAO urlaubskontoDAO;
    private HolidayEntitlementDAO urlaubsanspruchDAO;
    private OwnCalendarService calendarService;
    private HolidaysAccountService kontoService;

    public HolidaysAccountServiceImplTest() {

        urlaubskontoDAO = Mockito.mock(HolidaysAccountDAO.class);
        urlaubsanspruchDAO = Mockito.mock(HolidayEntitlementDAO.class);
        calendarService = Mockito.mock(OwnCalendarService.class);
//        kontoService = new HolidaysAccountServiceImpl(urlaubskontoDAO, urlaubsanspruchDAO, calendarService);
    }

    /** Test of newUrlaubsanspruch method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testNewUrlaubsanspruch() {

        Mockito.when(urlaubsanspruchDAO.save((HolidayEntitlement) (Mockito.any()))).thenAnswer(new Answer() {

                @Override
                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    HolidayEntitlement catchedObject = (HolidayEntitlement) (args[0]);
                    assertEquals("petar", catchedObject.getPerson().getFirstName());
                    assertEquals(new Double(30.0), catchedObject.getVacationDays());
                    assertEquals(new Integer(2000), catchedObject.getYear());

                    return null;
                }
            });

        Person person = new Person();
        person.setFirstName("petar");

//        kontoService.newUrlaubsanspruch(person, 2000, 30.0);
    }


    /** Test of saveUrlaubsanspruch method, of class HolidaysAccountServiceImpl. */
    @Test
    public void testSaveUrlaubsanspruch() {

        Mockito.when(urlaubsanspruchDAO.save((HolidayEntitlement) (Mockito.any()))).thenAnswer(new Answer() {

                @Override
                public Object answer(InvocationOnMock invocation) {

                    Object[] args = invocation.getArguments();
                    HolidayEntitlement catchedObject = (HolidayEntitlement) (args[0]);
                    assertEquals("petar", catchedObject.getPerson().getFirstName());
                    assertEquals(new Double(30.0), catchedObject.getVacationDays());
                    assertEquals(new Integer(2000), catchedObject.getYear());

                    return null;
                }
            });

        Person person = new Person();
        person.setFirstName("petar");
//
//        HolidayEntitlement anspruch = new HolidayEntitlement();
//        anspruch.setPerson(person);
//        anspruch.setVacationDays(30.0);
//        anspruch.setYear(2000);
//
//        kontoService.saveUrlaubsanspruch(anspruch);
    }

//
//    /** Test of saveUrlaubskonto method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testSaveUrlaubskonto() {
//
//        Mockito.when(urlaubskontoDAO.save((HolidaysAccount) (Mockito.any()))).thenAnswer(new Answer() {
//
//                @Override
//                public Object answer(InvocationOnMock invocation) {
//
//                    Object[] args = invocation.getArguments();
//                    HolidaysAccount catchedObject = (HolidaysAccount) (args[0]);
//                    assertEquals("petar", catchedObject.getPerson().getFirstName());
//                    assertEquals(new Double(30.0), catchedObject.getVacationDays());
//                    assertEquals(new Double(5.0), catchedObject.getRestVacationDays());
//                    assertEquals(new Integer(2000), catchedObject.getYear());
//
//                    return null;
//                }
//            });
//
//        Person person = new Person();
//        person.setFirstName("petar");
//
//        HolidaysAccount konto = new HolidaysAccount();
//        konto.setPerson(person);
//        konto.setVacationDays(30.0);
//        konto.setYear(2000);
//        konto.setRestVacationDays(5.0);
//
//        kontoService.saveUrlaubskonto(konto);
//    }
//
//
//    /** Test of newUrlaubskonto method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testNewUrlaubskonto() {
//
//        Mockito.when(urlaubskontoDAO.save((HolidaysAccount) (Mockito.any()))).thenAnswer(new Answer() {
//
//                @Override
//                public Object answer(InvocationOnMock invocation) {
//
//                    Object[] args = invocation.getArguments();
//                    HolidaysAccount catchedObject = (HolidaysAccount) (args[0]);
//                    assertEquals("petar", catchedObject.getPerson().getFirstName());
//                    assertEquals(new Double(30.0), catchedObject.getVacationDays());
//                    assertEquals(new Double(5.0), catchedObject.getRestVacationDays());
//                    assertEquals(new Integer(2000), catchedObject.getYear());
//
//                    return null;
//                }
//            });
//
//        Person person = new Person();
//        person.setFirstName("petar");
//
//        kontoService.newUrlaubskonto(person, 30.0, 5.0, 2000);
//    }
//
//
//    /** Test of getUrlaubsanspruch method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testGetUrlaubsanspruch() {
//
//        Mockito.when(urlaubsanspruchDAO.getUrlaubsanspruchByYear(Mockito.anyInt(), (Person) (Mockito.any())))
//            .thenAnswer(new Answer() {
//
//                    @Override
//                    public Object answer(InvocationOnMock invocation) {
//
//                        Object[] args = invocation.getArguments();
//                        Integer year = (Integer) (args[0]);
//                        Person person = (Person) (args[1]);
//                        assertEquals("petar", person.getFirstName());
//                        assertEquals(new Integer(2000), year);
//
//                        return null;
//                    }
//                });
//
//        Person person = new Person();
//        person.setFirstName("petar");
//
//        kontoService.getUrlaubsanspruch(2000, person);
//    }
//
//
//    /** Test of getUrlaubskonto method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testGetUrlaubskonto() {
//
//        Mockito.when(urlaubskontoDAO.getUrlaubskontoForDateAndPerson(Mockito.anyInt(), (Person) (Mockito.any())))
//            .thenAnswer(new Answer() {
//
//                    @Override
//                    public Object answer(InvocationOnMock invocation) {
//
//                        Object[] args = invocation.getArguments();
//                        Integer year = (Integer) (args[0]);
//                        Person person = (Person) (args[1]);
//                        assertEquals("petar", person.getFirstName());
//                        assertEquals(new Integer(2000), year);
//
//                        return null;
//                    }
//                });
//
//        Person person = new Person();
//        person.setFirstName("petar");
//
//        kontoService.getUrlaubskonto(2000, person);
//    }
//
//
//    /** Test of getUrlaubskontoForYear method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testGetUrlaubskontoForYear() {
//
//        Mockito.when(urlaubskontoDAO.getUrlaubskontoForYear(Mockito.anyInt())).thenAnswer(new Answer() {
//
//                @Override
//                public Object answer(InvocationOnMock invocation) {
//
//                    Object[] args = invocation.getArguments();
//                    Integer year = (Integer) (args[0]);
//                    assertEquals(new Integer(2000), year);
//
//                    return null;
//                }
//            });
//
//        kontoService.getUrlaubskontoForYear(2000);
//    }
//
//
//    /** Test of rollbackUrlaub method, of class HolidaysAccountServiceImpl. */
//    @Test
//    public void testRollbackUrlaub() {
//
//        HolidaysAccountService spy = Mockito.spy(kontoService);
//        HolidayEntitlement urlaubsanspruch = new HolidayEntitlement();
//        urlaubsanspruch.setVacationDays(20.0);
//
//        Application antrag = new Application();
//        antrag.setStartDate(new DateMidnight(2000, 12, 20));
//        antrag.setEndDate(new DateMidnight(2001, 1, 10));
//
//        // Mockito.when(spy.getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()))).thenReturn(urlaubsanspruch);
//        Mockito.doReturn(urlaubsanspruch).when(spy).getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()));
//        Mockito.doNothing().when(spy).rollbackNoticeJanuary((Application) (Mockito.any()),
//            (HolidaysAccount) (Mockito.any()), (HolidaysAccount) (Mockito.any()), Mockito.anyDouble(),
//            Mockito.anyDouble());
//
//        spy.rollbackUrlaub(antrag);
//
//        // ks should call rollbacknoticejanuary
//        Mockito.verify(spy).rollbackNoticeJanuary((Application) (Mockito.any()), (HolidaysAccount) (Mockito.any()),
//            (HolidaysAccount) (Mockito.any()), Mockito.anyDouble(), Mockito.anyDouble());
//
//        antrag.setStartDate(new DateMidnight(2001, 3, 20));
//        antrag.setEndDate(new DateMidnight(2001, 3, 22));
//
//        // Mockito.when(spy.getUrlaubsanspruch(Mockito.anyInt(), (Person)(Mockito.any()))).thenReturn(urlaubsanspruch);
//        Mockito.doReturn(urlaubsanspruch).when(spy).getUrlaubsanspruch(Mockito.anyInt(), (Person) (Mockito.any()));
//        Mockito.doNothing().when(spy).rollbackNoticeApril((Application) (Mockito.any()),
//            (HolidaysAccount) (Mockito.any()), Mockito.anyDouble());
//        spy.rollbackUrlaub(antrag);
//
//        // ks should call rollbacknoticeapril
//        Mockito.verify(spy).rollbackNoticeApril((Application) (Mockito.any()), (HolidaysAccount) (Mockito.any()),
//            Mockito.anyDouble());
//    }

    /** Test of rollbackNoticeJanuary method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testRollbackNoticeJanuary() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
// HolidaysAccount kontoNextYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(5.0);
// Mockito.when(kontoNextYear.getVacationDays()).thenReturn(15.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(kontoNextYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 12, 20));
// antrag.setEndDate(new DateMidnight(2001, 1, 10));
//
// kontoService.rollbackNoticeJanuary(antrag, kontoThisYear, kontoNextYear, 30.0, 30.0);
//
// Mockito.verify(kontoThisYear).setVacationDays(10.0);
// Mockito.verify(kontoNextYear).setVacationDays(20.0);
// }
//
// /** Test of rollbackNoticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testRollbackNoticeAprilAfter() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(22.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 6, 20));
// antrag.setEndDate( new DateMidnight(2001, 6, 30));
//
// kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
//
// Mockito.verify(kontoThisYear).setVacationDays(27.0);
// }
//
// /** Test of rollbackNoticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testRollbackNoticeAprilBefore() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(28.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 3, 20));
// antrag.setEndDate(new DateMidnight(2001, 3, 30));
//
// kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
//
// Mockito.verify(kontoThisYear).setVacationDays(30.0);
// Mockito.verify(kontoThisYear).setRestVacationDays(3.0);
// }
//
// /** Test of rollbackNoticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testRollbackNoticeAprilOva() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(22.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(1.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 3, 20));
// antrag.setEndDate(new DateMidnight(2001, 4, 10));
//
// kontoService.rollbackNoticeApril(antrag, kontoThisYear, 30.0);
//
// Mockito.verify(kontoThisYear).setVacationDays(30.0);
// Mockito.verify(kontoThisYear).setRestVacationDays(3.0);
// }
//
// /** Test of noticeJanuary method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testNoticeJanuary() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
// HolidaysAccount kontoNextYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(5.0);
// Mockito.when(kontoNextYear.getVacationDays()).thenReturn(15.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(kontoNextYear.getRestVacationDays()).thenReturn(0.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 12, 20));
// antrag.setEndDate(new DateMidnight(2001, 1, 10));
//
// kontoService.noticeJanuary(antrag, kontoThisYear, kontoNextYear);
//
// Mockito.verify(kontoThisYear).setVacationDays(0.0);
// Mockito.verify(kontoNextYear).setVacationDays(10.0);
// }
//
// /** Test of noticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testNoticeAprilBefore() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(2.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 3, 20));
// antrag.setEndDate(new DateMidnight(2001, 3, 30));
//
// kontoService.noticeApril(antrag, kontoThisYear);
//
// Mockito.verify(kontoThisYear).setVacationDays(27.0);
// Mockito.verify(kontoThisYear).setRestVacationDays(0.0);
// }
//
// /** Test of noticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testNoticeAprilAfter() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(2.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate(new DateMidnight(2000, 6, 20));
// antrag.setEndDate(new DateMidnight(2001, 6, 30));
//
// kontoService.noticeApril(antrag, kontoThisYear);
//
// Mockito.verify(kontoThisYear).setVacationDays(25.0);
// }
//
// /** Test of noticeApril method, of class HolidaysAccountServiceImpl. */
// @Test
// public void testNoticeAprilOva() {
//
// HolidaysAccount kontoThisYear = Mockito.mock(HolidaysAccount.class);
//
// Mockito.when(kontoThisYear.getVacationDays()).thenReturn(30.0);
// Mockito.when(kontoThisYear.getRestVacationDays()).thenReturn(7.0);
// Mockito.when(calendarService.getVacationDays((DateMidnight)(Mockito.any()),(DateMidnight)(Mockito.any()))).thenReturn(5.0);
//
// Application antrag = new Application();
// antrag.setStartDate( new DateMidnight(2000, 3, 20));
// antrag.setEndDate( new DateMidnight(2001, 4, 30));
//
// kontoService.noticeApril(antrag, kontoThisYear);
//
// Mockito.verify(kontoThisYear).setVacationDays(25.0);
// Mockito.verify(kontoThisYear).setRestVacationDays(2.0);
// }
}
