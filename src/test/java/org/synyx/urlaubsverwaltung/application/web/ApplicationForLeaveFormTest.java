package org.synyx.urlaubsverwaltung.application.web;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationForLeaveFormTest {

    private final Clock clock = Clock.systemUTC();

    @Test
    public void ensureGeneratedFullDayApplicationForLeaveHasCorrectPeriod() {

        LocalDate startDate = LocalDate.now(clock);
        LocalDate endDate = startDate.plusDays(3);

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

        LocalDate now = LocalDate.now(clock);

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

    @Test
    public void ensureBuilderSetsAllPropertiesCorrectly() {

        final Person person = new Person();
        final Person holidayReplacement = new Person();

        final LocalDate startDate = LocalDate.now().minusDays(10);
        final Time startTime = Time.valueOf(LocalTime.now().minusHours(5));

        final LocalDate endDate = LocalDate.now().minusDays(2);
        final Time endTime = Time.valueOf(LocalTime.now().minusHours(7));

        final VacationType vacationType = new VacationType();

        ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationType)
            .dayLength(DayLength.ZERO)
            .hours(BigDecimal.ZERO)
            .reason("Good one.")
            .holidayReplacement(holidayReplacement)
            .address("Gartenstrasse 67")
            .teamInformed(true)
            .comment("Welcome!")
            .build();

        assertThat(form.getPerson()).isEqualTo(person);
        assertThat(form.getStartDate()).isEqualTo(startDate);
        assertThat(form.getStartTime()).isEqualTo(startTime);
        assertThat(form.getEndDate()).isEqualTo(endDate);
        assertThat(form.getEndTime()).isEqualTo(endTime);
        assertThat(form.getVacationType()).isEqualTo(vacationType);
        assertThat(form.getDayLength()).isEqualTo(DayLength.ZERO);
        assertThat(form.getHours()).isEqualTo(BigDecimal.ZERO);
        assertThat(form.getReason()).isEqualTo("Good one.");
        assertThat(form.getHolidayReplacement()).isEqualTo(holidayReplacement);
        assertThat(form.getAddress()).isEqualTo("Gartenstrasse 67");
        assertThat(form.isTeamInformed()).isEqualTo(true);
        assertThat(form.getComment()).isEqualTo("Welcome!");
    }

    @Test
    public void toStringTest() {

        final Person person = new Person();
        final Person holidayReplacement = new Person();

        final LocalDate startDate = LocalDate.MIN;
        final Time startTime = Time.valueOf(LocalTime.MIN);

        final LocalDate endDate = LocalDate.MAX;
        final Time endTime = Time.valueOf(LocalTime.MAX);

        final VacationType vacationType = new VacationType();

        final ApplicationForLeaveForm form = new ApplicationForLeaveForm.Builder()
            .person(person)
            .startDate(startDate)
            .startTime(startTime)
            .endDate(endDate)
            .endTime(endTime)
            .vacationType(vacationType)
            .dayLength(DayLength.ZERO)
            .hours(BigDecimal.ZERO)
            .reason("Reason")
            .holidayReplacement(holidayReplacement)
            .address("Address")
            .teamInformed(true)
            .comment("Comment")
            .build();

        assertThat(form.toString()).isEqualTo("ApplicationForLeaveForm{person=Person{id='null'}, startDate=-999999999-01-01, " +
            "startTime=00:00:00, endDate=+999999999-12-31, endTime=23:59:59, " +
            "vacationType=VacationType{category=null, messageKey='null'}, dayLength=ZERO, hours=0, " +
            "holidayReplacement=Person{id='null'}, address='Address', teamInformed=true}");
    }
}
