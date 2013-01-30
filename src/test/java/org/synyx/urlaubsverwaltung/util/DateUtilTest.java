
package org.synyx.urlaubsverwaltung.util;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DateUtil}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class DateUtilTest {
    
    @Test
    public void testIsCorpusChristi2013() {
        
        DateMidnight date = new DateMidnight(2013, DateTimeConstants.MAY, 30);
        
        boolean returnValue = DateUtil.isCorpusChristi(date);
        
        Assert.assertTrue(returnValue);
        
    }
    
    @Test
    public void testIsCorpusChristi2014() {
        
        DateMidnight date = new DateMidnight(2014, DateTimeConstants.JUNE, 19);
        
        boolean returnValue = DateUtil.isCorpusChristi(date);
        
        Assert.assertTrue(returnValue);
        
    }
    
    @Test
    public void testIsCorpusChristi2015() {
        
        DateMidnight date = new DateMidnight(2015, DateTimeConstants.JUNE, 4);
        
        boolean returnValue = DateUtil.isCorpusChristi(date);
        
        Assert.assertTrue(returnValue);
        
    }
    
    @Test
    public void testIsNotCorpusChristi() {
        
        DateMidnight date = new DateMidnight(2012, DateTimeConstants.DECEMBER, 21);
        
        boolean returnValue = DateUtil.isCorpusChristi(date);
        
        Assert.assertFalse(returnValue);
        
    }
    
    @Test
    public void testIsWorkDay() {
        
        // should be a Monday
        DateMidnight date = new DateMidnight(2011, 12, 26);
        
        boolean returnValue = DateUtil.isWorkDay(date);
        
        Assert.assertTrue(returnValue);
        
    }
    
}
