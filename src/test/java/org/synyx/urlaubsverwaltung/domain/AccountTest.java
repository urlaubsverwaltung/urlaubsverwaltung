
package org.synyx.urlaubsverwaltung.domain;

import java.math.BigDecimal;
import junit.framework.Assert;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for domain class {@link Account}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class AccountTest {
    
    private Person person;
    
    @Before
    public void setup() {
        
        person = new Person();
        
    }
    
    
    /**
     * Test of calculateActualVacationDays method, of class Account.
     */
    @Test
    public void testCalculateActualVacationDaysGreaterThanHalf() {
        
        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.AUGUST, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);
        
        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);
        
        account.calculateActualVacationDays();
        
        Assert.assertEquals(BigDecimal.valueOf(28), account.getAnnualVacationDays());
        Assert.assertEquals(BigDecimal.ZERO, account.getRemainingVacationDays());
        Assert.assertEquals(BigDecimal.valueOf(12), account.getVacationDays());
        
    }
    
    @Test
    public void testCalculateActualVacationDaysBetweenHalf() {
        
        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);
        
        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(28), BigDecimal.ZERO, true);
        
        account.calculateActualVacationDays();
        
        Assert.assertEquals(BigDecimal.valueOf(28), account.getAnnualVacationDays());
        Assert.assertEquals(BigDecimal.ZERO, account.getRemainingVacationDays());
        Assert.assertEquals(BigDecimal.valueOf(9.5), account.getVacationDays());
        
    }
    
    @Test
    public void testCalculateActualVacationDaysAlmostZero() {
        
        DateMidnight startDate = new DateMidnight(2012, DateTimeConstants.SEPTEMBER, 1);
        DateMidnight endDate = new DateMidnight(2012, DateTimeConstants.DECEMBER, 31);
        
        Account account = new Account(person, startDate.toDate(), endDate.toDate(), BigDecimal.valueOf(33.3), BigDecimal.ZERO, true);
        
        account.calculateActualVacationDays();
        
        Assert.assertEquals(BigDecimal.valueOf(33.3), account.getAnnualVacationDays());
        Assert.assertEquals(BigDecimal.ZERO, account.getRemainingVacationDays());
        Assert.assertEquals(BigDecimal.valueOf(11), account.getVacationDays());
        
    }
 
}
