package org.synyx.urlaubsverwaltung.calendar.workingtime;

import junit.framework.Assert;

import org.joda.time.DateTimeConstants;

import org.junit.Test;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;

import java.util.Arrays;
import java.util.List;


/**
 * Unit test for {@link WorkingTime}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeTest {

    @Test
    public void testHasWorkingDaysIdentical() {

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);

        List<Integer> workingDaysToCompare = Arrays.asList(DateTimeConstants.FRIDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.MONDAY, DateTimeConstants.THURSDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);

        Assert.assertTrue("Working days are not identical", returnValue);
    }


    @Test
    public void testHasWorkingDaysDifferent() {

        List<Integer> workingDays = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);

        List<Integer> workingDaysToCompare = Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.SUNDAY);

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);

        Assert.assertFalse("Working days are identical", returnValue);
    }
}
