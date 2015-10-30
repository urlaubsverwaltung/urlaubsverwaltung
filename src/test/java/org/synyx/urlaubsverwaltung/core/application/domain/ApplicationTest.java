package org.synyx.urlaubsverwaltung.core.application.domain;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.period.Period;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.application.domain.Application}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationTest {

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
}
