
package org.synyx.urlaubsverwaltung.util;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DateUtil}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class DateUtilTest {
    
    
    @Test
    public void testIsWorkDay() {
        
        // should be a Monday
        DateMidnight date = new DateMidnight(2011, 12, 26);
        
        boolean returnValue = DateUtil.isWorkDay(date);
        
        Assert.assertTrue(returnValue);
        
    }
    
}
