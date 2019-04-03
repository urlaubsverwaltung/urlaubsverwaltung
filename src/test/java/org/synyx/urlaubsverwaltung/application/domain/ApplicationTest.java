package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;

import java.sql.Time;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;


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
        application.setEndDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutEndDate() {

        Application application = new Application();
        application.setStartDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setEndDate(null);
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutDayLength() {

        Application application = new Application();
        application.setStartDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setEndDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setDayLength(null);

        application.getPeriod();
    }


    @Test
    public void ensureGetPeriodReturnsCorrectPeriod() {

        LocalDate startDate = ZonedDateTime.now(UTC).toLocalDate();
        LocalDate endDate = startDate.plusDays(2);

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

        LocalDate startDate = LocalDate.of(2016, 2, 1);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setStartTime(Time.valueOf("11:15:00"));

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNotNull("Should not be null", startDateWithTime);
        Assert.assertEquals("Wrong start date with time", ZonedDateTime.of(2016, 2, 1, 11, 15, 0, 0, startDateWithTime.getZone()), startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartTimeIsNull() {

        Application application = new Application();
        application.setStartDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setStartTime(null);

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartDateIsNull() {

        Application application = new Application();
        application.setStartDate(null);
        application.setStartTime(Time.valueOf("10:15:00"));

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsCorrectDateTime() {

        LocalDate endDate = LocalDate.of(2016, 12, 21);

        Application application = new Application();
        application.setEndDate(endDate);
        application.setEndTime(Time.valueOf("12:30:00"));

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNotNull("Should not be null", endDateWithTime);
        Assert.assertEquals("Wrong end date with time", ZonedDateTime.of(2016, 12, 21, 12, 30, 0, 0, endDateWithTime.getZone()), endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndTimeIsNull() {

        Application application = new Application();
        application.setEndDate(ZonedDateTime.now(UTC).toLocalDate());
        application.setEndTime(null);

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndDateIsNull() {

        Application application = new Application();
        application.setEndDate(null);
        application.setEndTime(Time.valueOf("10:15:00"));

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }
}
