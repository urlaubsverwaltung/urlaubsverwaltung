package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveFormTest {

    @Test
    public void ensureGeneratedFullDayApplicationForLeaveHasCorrectPeriod() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(3);
        DateMidnight someOtherDate = startDate.minusDays(10);

        ApplicationForLeaveForm form = new ApplicationForLeaveForm();

        form.setDayLength(DayLength.FULL);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setStartDateHalf(someOtherDate);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong start date", startDate, application.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, application.getEndDate());
    }


    @Test
    public void ensureGeneratedHalfDayApplicationForLeaveHasCorrectPeriod() {

        DateMidnight now = DateMidnight.now();
        DateMidnight someOtherDate = now.minusDays(10);

        ApplicationForLeaveForm form = new ApplicationForLeaveForm();

        form.setDayLength(DayLength.MORNING);
        form.setStartDateHalf(now);
        form.setStartDate(someOtherDate);
        form.setEndDate(someOtherDate);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong start date", now, application.getStartDate());
        Assert.assertEquals("Wrong end date", now, application.getEndDate());
    }


    @Test
    public void ensureGeneratedApplicationForLeaveHasCorrectProperties() {

        Person person = TestDataCreator.createPerson();
        Person holidayReplacement = TestDataCreator.createPerson("vertretung");

        ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setPerson(person);
        form.setDayLength(DayLength.FULL);
        form.setAddress("Musterstr. 39");
        form.setComment("Kommentar");
        form.setHolidayReplacement(holidayReplacement);
        form.setReason("Deshalb");
        form.setTeamInformed(true);
        form.setVacationType(VacationType.OVERTIME);
        form.setHours(BigDecimal.ONE);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong person", person, application.getPerson());
        Assert.assertEquals("Wrong holiday replacement", holidayReplacement, application.getHolidayReplacement());
        Assert.assertEquals("Wrong day length", DayLength.FULL, application.getDayLength());
        Assert.assertEquals("Wrong address", "Musterstr. 39", application.getAddress());
        Assert.assertEquals("Wrong reason", "Deshalb", application.getReason());
        Assert.assertEquals("Wrong type", VacationType.OVERTIME, application.getVacationType());
        Assert.assertEquals("Wrong hours", BigDecimal.ONE, application.getHours());
        Assert.assertTrue("Team should be informed", application.isTeamInformed());
    }
}
