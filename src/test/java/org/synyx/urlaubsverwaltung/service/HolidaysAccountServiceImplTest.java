package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import junit.framework.Assert;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 * Unit test for {@link HolidaysAccountServiceImpl}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class HolidaysAccountServiceImplTest {

    private HolidaysAccountServiceImpl service;
    private AccountDAO accountDAO;
    private OwnCalendarService calendarService;
    private Account account;
    private Person person;

    @Before
    public void setup() {

        accountDAO = Mockito.mock(AccountDAO.class);
        calendarService = new OwnCalendarService();
        service = new HolidaysAccountServiceImpl(accountDAO, calendarService);

        person = new Person();
        person.setLoginName("horscht");

        DateMidnight validFrom = new DateMidnight(2012, DateTimeConstants.JANUARY, 1);
        DateMidnight validTo = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, validFrom.toDate(), validTo.toDate(), BigDecimal.valueOf(28), BigDecimal.valueOf(5), true);

    }

    @Test
    public void testGetAccount() {

        Mockito.when(accountDAO.getHolidaysAccountByYearAndPerson(2012, person)).thenReturn(account);

        Account result = service.getHolidaysAccount(2012, person);

        Assert.assertNotNull(result);
        Assert.assertEquals(account, result);
    }

    @Test
    public void testCalculateActualVacationDaysGreaterThanHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.AUGUST, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(12), result);

    }

    @Test
    public void testCalculateActualVacationDaysBetweenHalf() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(9.5), result);

    }

    @Test
    public void testCalculateActualVacationDaysAlmostZero() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(33.3), BigDecimal.ZERO, true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(11), result);

    }

    @Test
    public void testCalculateActualVacationDaysForHalfMonths1() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.MAY, 15);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(18), result);
    }
    
    @Test
    public void testCalculateActualVacationDaysForHalfMonths2() {

        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.JULY, 16);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);

        account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);

        BigDecimal result = service.calculateActualVacationDays(account);

        Assert.assertEquals(BigDecimal.valueOf(13.00), result);
    }
}
