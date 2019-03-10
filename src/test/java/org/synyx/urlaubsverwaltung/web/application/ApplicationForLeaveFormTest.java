package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;
import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationForLeaveFormTest {

    @Test
    public void ensureGeneratedFullDayApplicationForLeaveHasCorrectPeriod() {

        DateMidnight startDate = DateMidnight.now();
        DateMidnight endDate = startDate.plusDays(3);

        ApplicationForLeaveForm form = new ApplicationForLeaveForm();

        form.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        form.setDayLength(DayLength.FULL);
        form.setStartDate(startDate);
        form.setEndDate(endDate);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong start date", startDate, application.getStartDate());
        Assert.assertEquals("Wrong end date", endDate, application.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.FULL, application.getDayLength());
    }


    @Test
    public void ensureGeneratedHalfDayApplicationForLeaveHasCorrectPeriod() {

        DateMidnight now = DateMidnight.now();

        ApplicationForLeaveForm form = new ApplicationForLeaveForm();
        form.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        form.setDayLength(DayLength.MORNING);
        form.setStartDate(now);
        form.setEndDate(now);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong start date", now, application.getStartDate());
        Assert.assertEquals("Wrong end date", now, application.getEndDate());
        Assert.assertEquals("Wrong day length", DayLength.MORNING, application.getDayLength());
    }


    @Test
    public void ensureGeneratedApplicationForLeaveHasCorrectProperties() {

        VacationType overtime = TestDataCreator.createVacationType(VacationCategory.OVERTIME);

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
        form.setVacationType(overtime);
        form.setHours(BigDecimal.ONE);

        Application application = form.generateApplicationForLeave();

        Assert.assertEquals("Wrong person", person, application.getPerson());
        Assert.assertEquals("Wrong holiday replacement", holidayReplacement, application.getHolidayReplacement());
        Assert.assertEquals("Wrong day length", DayLength.FULL, application.getDayLength());
        Assert.assertEquals("Wrong address", "Musterstr. 39", application.getAddress());
        Assert.assertEquals("Wrong reason", "Deshalb", application.getReason());
        Assert.assertEquals("Wrong type", overtime.getMessageKey(), application.getVacationType().getMessageKey());
        Assert.assertEquals("Wrong hours", BigDecimal.ONE, application.getHours());
        Assert.assertTrue("Team should be informed", application.isTeamInformed());
    }


    @Test
    public void ensureGeneratedApplicationForLeaveHasNullHoursForOtherVacationTypeThanOvertime() {

        Consumer<VacationType> assertHoursAreNotSet = (type) -> {
            ApplicationForLeaveForm form = new ApplicationForLeaveForm();
            form.setVacationType(type);
            form.setHours(BigDecimal.ONE);

            Application application = form.generateApplicationForLeave();

            Assert.assertNull("Hours should not be set", application.getHours());
        };

        VacationType holiday = TestDataCreator.createVacationType(VacationCategory.HOLIDAY);
        VacationType specialLeave = TestDataCreator.createVacationType(VacationCategory.SPECIALLEAVE);
        VacationType unpaidLeave = TestDataCreator.createVacationType(VacationCategory.UNPAIDLEAVE);

        assertHoursAreNotSet.accept(holiday);
        assertHoursAreNotSet.accept(specialLeave);
        assertHoursAreNotSet.accept(unpaidLeave);
    }
}
