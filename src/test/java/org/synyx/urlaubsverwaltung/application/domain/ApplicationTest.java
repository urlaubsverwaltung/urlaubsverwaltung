package org.synyx.urlaubsverwaltung.application.domain;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;

import java.sql.Time;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.application.domain.Application}.
 */
public class ApplicationTest {

    // Status ----------------------------------------------------------------------------------------------------------

    @Test
    public void ensureReturnsTrueIfItHasTheGivenStatus() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.ALLOWED);

        Assert.assertTrue("Should return true if it has the given status",
            application.hasStatus(ApplicationStatus.ALLOWED));
    }


    @Test
    public void ensureReturnsFalseIfItHasNotTheGivenStatus() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.CANCELLED);

        Assert.assertFalse("Should return false if it has the given status",
            application.hasStatus(ApplicationStatus.ALLOWED));
    }


    // Formerly allowed ------------------------------------------------------------------------------------------------

    @Test
    public void ensureIsFormerlyAllowedReturnsFalseIfIsRevoked() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.REVOKED);

        Assert.assertFalse("Should not be formerly allowed", application.isFormerlyAllowed());
    }


    @Test
    public void ensureIsFormerlyAllowedReturnsTrueIfIsCancelled() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.CANCELLED);

        Assert.assertTrue("Should be formerly allowed", application.isFormerlyAllowed());
    }


    // Period ----------------------------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutStartDate() {

        Application application = new Application();
        application.setStartDate(null);
        application.setEndDate(DateMidnight.now());
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutEndDate() {

        Application application = new Application();
        application.setStartDate(DateMidnight.now());
        application.setEndDate(null);
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutDayLength() {

        Application application = new Application();
        application.setStartDate(DateMidnight.now());
        application.setEndDate(DateMidnight.now());
        application.setDayLength(null);

        application.getPeriod();
    }


    @Test
    public void ensureGetPeriodReturnsCorrectPeriod() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(2);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);

        Period period = application.getPeriod();

        Assert.assertNotNull("Period should not be null", period);
        Assert.assertEquals("Wrong period start date", startDate, period.getStartDate());
        Assert.assertEquals("Wrong period end date", endDate, period.getEndDate());
        Assert.assertEquals("Wrong period day length", DayLength.FULL, period.getDayLength());
    }


    // Start and end time ----------------------------------------------------------------------------------------------

    @Test
    public void ensureGetStartDateWithTimeReturnsCorrectDateTime() {

        DateMidnight startDate = new DateMidnight(2016, 2, 1);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setStartTime(Time.valueOf("11:15:00"));

        DateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNotNull("Should not be null", startDateWithTime);
        Assert.assertEquals("Wrong start date with time", new DateTime(2016, 2, 1, 11, 15, 0), startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartTimeIsNull() {

        Application application = new Application();
        application.setStartDate(DateMidnight.now());
        application.setStartTime(null);

        DateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartDateIsNull() {

        Application application = new Application();
        application.setStartDate(null);
        application.setStartTime(Time.valueOf("10:15:00"));

        DateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsCorrectDateTime() {

        DateMidnight endDate = new DateMidnight(2016, 12, 21);

        Application application = new Application();
        application.setEndDate(endDate);
        application.setEndTime(Time.valueOf("12:30:00"));

        DateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNotNull("Should not be null", endDateWithTime);
        Assert.assertEquals("Wrong end date with time", new DateTime(2016, 12, 21, 12, 30, 0), endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndTimeIsNull() {

        Application application = new Application();
        application.setEndDate(DateMidnight.now());
        application.setEndTime(null);

        DateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndDateIsNull() {

        Application application = new Application();
        application.setEndDate(null);
        application.setEndTime(Time.valueOf("10:15:00"));

        DateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }
}
