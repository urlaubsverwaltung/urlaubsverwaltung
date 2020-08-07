package org.synyx.urlaubsverwaltung.application.domain;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


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
        application.setEndDate(Instant.now());
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutEndDate() {

        Application application = new Application();
        application.setStartDate(Instant.now());
        application.setEndDate(null);
        application.setDayLength(DayLength.FULL);

        application.getPeriod();
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetPeriodForApplicationWithoutDayLength() {

        Application application = new Application();
        application.setStartDate(Instant.now());
        application.setEndDate(Instant.now());
        application.setDayLength(null);

        application.getPeriod();
    }


    @Test
    public void ensureGetPeriodReturnsCorrectPeriod() {

        Instant startDate = Instant.now();
        Instant endDate = startDate.plus(2, DAYS);

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

        Instant startDate = Instant.from(LocalDate.of(2016, 2, 1));

        Application application = new Application();
        application.setStartDate(startDate);
        application.setStartTime(Time.valueOf("11:15:00"));

        Instant startDateWithTime = application.getStartDateWithTime();

        Assert.assertNotNull("Should not be null", startDateWithTime);
        Assert.assertEquals("Wrong start date with time", ZonedDateTime.of(2016, 2, 1, 11, 15, 0, 0, ZoneId.from(startDateWithTime)), startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartTimeIsNull() {

        Application application = new Application();
        application.setStartDate(Instant.now());
        application.setStartTime(null);

        Instant startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetStartDateWithTimeReturnsNullIfStartDateIsNull() {

        Application application = new Application();
        application.setStartDate(null);
        application.setStartTime(Time.valueOf("10:15:00"));

        Instant startDateWithTime = application.getStartDateWithTime();

        Assert.assertNull("Should be null", startDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsCorrectDateTime() {

        Instant endDate = Instant.from(LocalDate.of(2016, 12, 21));

        Application application = new Application();
        application.setEndDate(endDate);
        application.setEndTime(Time.valueOf("12:30:00"));

        Instant endDateWithTime = application.getEndDateWithTime();

        Assert.assertNotNull("Should not be null", endDateWithTime);
        Assert.assertEquals("Wrong end date with time", ZonedDateTime.of(2016, 12, 21, 12, 30, 0, 0, ZoneId.from(endDateWithTime)), endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndTimeIsNull() {

        Application application = new Application();
        application.setEndDate(Instant.now());
        application.setEndTime(null);

        Instant endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }


    @Test
    public void ensureGetEndDateWithTimeReturnsNullIfEndDateIsNull() {

        Application application = new Application();
        application.setEndDate(null);
        application.setEndTime(Time.valueOf("10:15:00"));

        Instant endDateWithTime = application.getEndDateWithTime();

        Assert.assertNull("Should be null", endDateWithTime);
    }

    @Test
    public void toStringTest() {

        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));

        final VacationType vacationType = new VacationType();
        vacationType.setCategory(VacationCategory.HOLIDAY);

        final Application application = new Application();
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(Instant.MIN);
        application.setEndDate(Instant.MAX);
        application.setDayLength(DayLength.FULL);
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setId(1);
        application.setHours(BigDecimal.TEN);
        application.setApplicationDate(Instant.EPOCH);
        application.setTwoStageApproval(true);
        application.setHolidayReplacement(person);
        application.setApplier(person);
        application.setRemindDate(Instant.MAX);
        application.setBoss(person);
        application.setEditedDate(Instant.MAX);
        application.setEndTime(Time.valueOf(LocalTime.MAX));
        application.setStartTime(Time.valueOf(LocalTime.MIN));
        application.setCanceller(person);
        application.setReason("Because");
        application.setAddress("Address");
        application.setCancelDate(Instant.MAX);
        application.setTeamInformed(true);

        final String toString = application.toString();
        assertThat(toString).isEqualTo("Application{person=Person{id='10'}, applier=Person{id='10'}, " +
            "boss=Person{id='10'}, canceller=Person{id='10'}, twoStageApproval=true, startDate=-999999999-01-01, " +
            "startTime=00:00:00, endDate=+999999999-12-31, endTime=23:59:59, " +
            "vacationType=VacationType{category=HOLIDAY, messageKey='null'}, dayLength=FULL, " +
            "holidayReplacement=Person{id='10'}, address='Address', applicationDate=1970-01-01, " +
            "cancelDate=+999999999-12-31, editedDate=+999999999-12-31, remindDate=+999999999-12-31, status=ALLOWED, " +
            "teamInformed=true, hours=10}");
    }
}
