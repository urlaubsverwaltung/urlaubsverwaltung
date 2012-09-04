
package org.synyx.urlaubsverwaltung.service;

import java.math.BigDecimal;
import junit.framework.Assert;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
    
    private Account account;
    private Person person;
    
    @Before
    public void setup() {
        
        accountDAO = Mockito.mock(AccountDAO.class);
        service = new HolidaysAccountServiceImpl(accountDAO);
        
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
    
}
