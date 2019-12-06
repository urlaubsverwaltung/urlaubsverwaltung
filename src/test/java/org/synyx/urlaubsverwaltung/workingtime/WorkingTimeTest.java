package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;


/**
 * Unit test for {@link WorkingTime}.
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

        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());

        List<Integer> workingDaysToCompare = Arrays.asList(FRIDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), MONDAY.getValue(), THURSDAY.getValue());

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);

        Assert.assertTrue("Working days are not identical", returnValue);
    }


    @Test
    public void testHasWorkingDaysDifferent() {

        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());

        List<Integer> workingDaysToCompare = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), SUNDAY.getValue());

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);

        Assert.assertFalse("Working days are identical", returnValue);
    }
}
