package org.synyx.urlaubsverwaltung.core.workingtime;

import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.period.DayLength;

import java.util.Arrays;
import java.util.List;


/**
 * Unit test for {@link WorkingTime}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class WorkingTimeTest {

    @Test
    public void testDefaultValues() {

        WorkingTime workingTime = new WorkingTime();

        Assert.assertEquals("Wrong day length for monday", DayLength.ZERO, workingTime.getMonday());
        Assert.assertEquals("Wrong day length for tuesday", DayLength.ZERO, workingTime.getTuesday());
        Assert.assertEquals("Wrong day length for wednesday", DayLength.ZERO, workingTime.getWednesday());
        Assert.assertEquals("Wrong day length for thursday", DayLength.ZERO, workingTime.getThursday());
        Assert.assertEquals("Wrong day length for friday", DayLength.ZERO, workingTime.getFriday());
        Assert.assertEquals("Wrong day length for saturday", DayLength.ZERO, workingTime.getSaturday());
        Assert.assertEquals("Wrong day length for sunday", DayLength.ZERO, workingTime.getSunday());

        Assert.assertFalse("There should be no federal state override",
            workingTime.getFederalStateOverride().isPresent());
    }


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
