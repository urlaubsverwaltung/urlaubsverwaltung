package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeEntity;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;


/**
 * Unit test for {@link Application}.
 */
class ApplicationTest {

    // Status ----------------------------------------------------------------------------------------------------------
    @Test
    void ensureReturnsTrueIfItHasTheGivenStatus() {

        Application application = new Application();
        application.setStatus(ApplicationStatus.ALLOWED);

        assertThat(application.hasStatus(ApplicationStatus.ALLOWED)).isTrue();
    }

    @Test
    void ensureReturnsFalseIfItHasNotTheGivenStatus() {

        Application application = new Application();
        application.setStatus(ApplicationStatus.CANCELLED);

        assertThat(application.hasStatus(ApplicationStatus.ALLOWED)).isFalse();
    }

    // Formerly allowed ------------------------------------------------------------------------------------------------
    @Test
    void ensureIsFormerlyAllowedReturnsFalseIfIsRevoked() {

        Application application = new Application();
        application.setStatus(ApplicationStatus.REVOKED);

        assertThat(application.isFormerlyAllowed()).isFalse();
    }

    @Test
    void ensureIsFormerlyAllowedReturnsTrueIfIsCancelled() {

        Application application = new Application();
        application.setStatus(ApplicationStatus.CANCELLED);

        assertThat(application.isFormerlyAllowed()).isTrue();
    }

    // Period ----------------------------------------------------------------------------------------------------------
    @Test
    void ensureGetPeriodReturnsCorrectPeriod() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(2);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setDayLength(DayLength.FULL);

        Period period = application.getPeriod();

        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
        assertThat(period.getDayLength()).isEqualTo(DayLength.FULL);
    }

    // Start and end time ----------------------------------------------------------------------------------------------
    @Test
    void ensureGetStartDateWithTimeReturnsCorrectDateTime() {

        LocalDate startDate = LocalDate.of(2016, 2, 1);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setStartTime(Time.valueOf("11:15:00"));

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        final ZonedDateTime expected = ZonedDateTime.of(2016, 2, 1, 11, 15, 0, 0, startDateWithTime.getZone());
        assertThat(startDateWithTime).isEqualTo(expected);
    }

    @Test
    void ensureGetStartDateWithTimeReturnsNullIfStartTimeIsNull() {

        Application application = new Application();
        application.setStartDate(LocalDate.now(UTC));
        application.setStartTime(null);

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        assertThat(startDateWithTime).isNull();
    }

    @Test
    void ensureGetStartDateWithTimeReturnsNullIfStartDateIsNull() {

        Application application = new Application();
        application.setStartDate(null);
        application.setStartTime(Time.valueOf("10:15:00"));

        ZonedDateTime startDateWithTime = application.getStartDateWithTime();

        assertThat(startDateWithTime).isNull();
    }

    @Test
    void ensureGetEndDateWithTimeReturnsCorrectDateTime() {

        LocalDate endDate = LocalDate.of(2016, 12, 21);

        Application application = new Application();
        application.setEndDate(endDate);
        application.setEndTime(Time.valueOf("12:30:00"));

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        final ZonedDateTime expected = ZonedDateTime.of(2016, 12, 21, 12, 30, 0, 0, endDateWithTime.getZone());
        assertThat(endDateWithTime).isEqualTo(expected);
    }

    @Test
    void ensureGetEndDateWithTimeReturnsNullIfEndTimeIsNull() {

        Application application = new Application();
        application.setEndDate(LocalDate.now(UTC));
        application.setEndTime(null);

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        assertThat(endDateWithTime).isNull();
    }

    @Test
    void ensureGetEndDateWithTimeReturnsNullIfEndDateIsNull() {

        Application application = new Application();
        application.setEndDate(null);
        application.setEndTime(Time.valueOf("10:15:00"));

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        assertThat(endDateWithTime).isNull();
    }

    @Test
    void toStringTest() {

        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(person);
        replacementEntity.setNote("hello myself");

        final VacationTypeEntity vacationType = new VacationTypeEntity();
        vacationType.setCategory(VacationCategory.HOLIDAY);

        final Application application = new Application();
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(DayLength.FULL);
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setId(1);
        application.setHours(Duration.ofHours(10));
        application.setApplicationDate(LocalDate.EPOCH);
        application.setTwoStageApproval(true);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setApplier(person);
        application.setRemindDate(LocalDate.MAX);
        application.setBoss(person);
        application.setEditedDate(LocalDate.MAX);
        application.setEndTime(Time.valueOf(LocalTime.MAX));
        application.setStartTime(Time.valueOf(LocalTime.MIN));
        application.setCanceller(person);
        application.setReason("Because");
        application.setAddress("Address");
        application.setCancelDate(LocalDate.MAX);
        application.setTeamInformed(true);

        final String toString = application.toString();
        assertThat(toString).isEqualTo("Application{person=Person{id='10'}, applier=Person{id='10'}, " +
            "boss=Person{id='10'}, canceller=Person{id='10'}, twoStageApproval=true, startDate=-999999999-01-01, " +
            "startTime=00:00:00, endDate=+999999999-12-31, endTime=23:59:59, " +
            "vacationType=VacationType{id=null, active=false, category=HOLIDAY, messageKey='null'}, dayLength=FULL, " +
            "holidayReplacements=[HolidayReplacement{, person=Person{id='10'}, note='hello myself'}], " +
            "address='Address', applicationDate=1970-01-01, cancelDate=+999999999-12-31, " +
            "editedDate=+999999999-12-31, remindDate=+999999999-12-31, status=ALLOWED, teamInformed=true, hours=PT10H}");
    }

    @Test
    void equals() {
        final Application applicationOne = new Application();
        applicationOne.setId(1);

        final Application applicationOneOne = new Application();
        applicationOneOne.setId(1);

        final Application applicationTwo = new Application();
        applicationTwo.setId(2);

        assertThat(applicationOne)
            .isEqualTo(applicationOne)
            .isEqualTo(applicationOneOne)
            .isNotEqualTo(applicationTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Application applicationOne = new Application();
        applicationOne.setId(1);

        assertThat(applicationOne.hashCode()).isEqualTo(32);
    }
}
