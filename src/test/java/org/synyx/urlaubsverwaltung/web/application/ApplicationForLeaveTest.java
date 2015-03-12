package org.synyx.urlaubsverwaltung.web.application;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.web.application.ApplicationForLeave}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveTest {

    @Test
    public void ensureCreatesCorrectApplicationForLeave() {

        OwnCalendarService calendarService = Mockito.mock(OwnCalendarService.class);

        DateMidnight startDate = new DateMidnight(2015, 3, 3);
        DateMidnight endDate = new DateMidnight(2015, 3, 6);
        DayLength dayLength = DayLength.FULL;

        Application application = new Application();
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setHowLong(dayLength);

        Mockito.when(calendarService.getWorkDays(Mockito.eq(dayLength), Mockito.eq(startDate), Mockito.eq(endDate),
                Mockito.any(Person.class))).thenReturn(BigDecimal.TEN);

        ApplicationForLeave applicationForLeave = new ApplicationForLeave(application, calendarService);

        Assert.assertNotNull("Should not be null", applicationForLeave.getStartDate());
        Assert.assertNotNull("Should not be null", applicationForLeave.getEndDate());
        Assert.assertNotNull("Should not be null", applicationForLeave.getHowLong());

        Assert.assertEquals("Wrong start date", startDate, applicationForLeave.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, applicationForLeave.getEndDate());
        Assert.assertEquals("Wrong day length", dayLength, applicationForLeave.getHowLong());

        Assert.assertNotNull("Should not be null", applicationForLeave.getWorkDays());
        Assert.assertEquals("Wrong number of work days", BigDecimal.TEN, applicationForLeave.getWorkDays());
    }
}
