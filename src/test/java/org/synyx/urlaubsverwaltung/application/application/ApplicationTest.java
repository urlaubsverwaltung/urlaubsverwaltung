package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofHours;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
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

        assertThat(period.startDate()).isEqualTo(startDate);
        assertThat(period.endDate()).isEqualTo(endDate);
        assertThat(period.dayLength()).isEqualTo(DayLength.FULL);
    }

    // Start and end time ----------------------------------------------------------------------------------------------
    @Test
    void ensureGetStartDateWithTimeReturnsCorrectDateTime() {

        LocalDate startDate = LocalDate.of(2016, 2, 1);

        Application application = new Application();
        application.setStartDate(startDate);
        application.setStartTime(LocalTime.of(11, 15, 0));

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

        final Application application = new Application();
        application.setStartDate(null);
        application.setStartTime(LocalTime.of(10, 15, 0));

        final ZonedDateTime startDateWithTime = application.getStartDateWithTime();
        assertThat(startDateWithTime).isNull();
    }

    @Test
    void ensureGetEndDateWithTimeReturnsCorrectDateTime() {

        final Application application = new Application();
        application.setEndDate(LocalDate.of(2016, 12, 21));
        application.setEndTime(LocalTime.of(12, 30, 0));

        final ZonedDateTime endDateWithTime = application.getEndDateWithTime();
        final ZonedDateTime expected = ZonedDateTime.of(2016, 12, 21, 12, 30, 0, 0, endDateWithTime.getZone());
        assertThat(endDateWithTime).isEqualTo(expected);
    }

    @Test
    void ensureGetEndDateWithTimeReturnsNullIfEndTimeIsNull() {

        final Application application = new Application();
        application.setEndDate(LocalDate.now(UTC));
        application.setEndTime(null);

        ZonedDateTime endDateWithTime = application.getEndDateWithTime();

        assertThat(endDateWithTime).isNull();
    }

    @Test
    void ensureGetEndDateWithTimeReturnsNullIfEndDateIsNull() {

        final Application application = new Application();
        application.setEndDate(null);
        application.setEndTime(LocalTime.of(10, 15, 0));

        final ZonedDateTime endDateWithTime = application.getEndDateWithTime();
        assertThat(endDateWithTime).isNull();
    }

    @Test
    void getHoursByYear() {

        final Application application = new Application();
        application.setStartDate(LocalDate.of(2022, 12, 30));
        application.setEndDate(LocalDate.of(2023, 1, 2));
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        application.setHours(Duration.ofHours(20));

        final Map<Integer, Duration> hoursByYear = application.getHoursByYear();

        assertThat(hoursByYear).hasSize(2)
            .containsEntry(2022, Duration.ofHours(10))
            .containsEntry(2023, Duration.ofHours(10));
    }

    @Test
    void toStringTest() {

        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10L);
        person.setPermissions(List.of(USER));

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(person);
        replacementEntity.setNote("hello myself");

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource())
            .id(1L)
            .category(VacationCategory.HOLIDAY)
            .color(YELLOW)
            .build();

        final Application application = new Application();
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(LocalDate.MIN);
        application.setEndDate(LocalDate.MAX);
        application.setDayLength(DayLength.FULL);
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setId(1L);
        application.setHours(Duration.ofHours(10));
        application.setApplicationDate(LocalDate.EPOCH);
        application.setTwoStageApproval(true);
        application.setHolidayReplacements(List.of(replacementEntity));
        application.setApplier(person);
        application.setRemindDate(LocalDate.MAX);
        application.setBoss(person);
        application.setEditedDate(LocalDate.MAX);
        application.setEndTime(LocalTime.of(11, 15, 0));
        application.setStartTime(LocalTime.of(12, 15, 0));
        application.setCanceller(person);
        application.setReason("Because");
        application.setAddress("Address");
        application.setCancelDate(LocalDate.MAX);
        application.setTeamInformed(true);

        final String toString = application.toString();
        assertThat(toString).isEqualTo("Application{person=Person{id='10'}, applier=Person{id='10'}, boss=Person{id='10'}, " +
            "canceller=Person{id='10'}, twoStageApproval=true, startDate=-999999999-01-01, startTime=12:15, endDate=+999999999-12-31, " +
            "endTime=11:15, vacationType=ProvidedVacationType{messageKey='null', id=1, active=false, category=HOLIDAY, " +
            "requiresApprovalToApply=false, requiresApprovalToCancel=false, color=YELLOW, visibleToEveryone=false}, dayLength=FULL, " +
            "holidayReplacements=[HolidayReplacementEntity{person=Person{id='10'}}], " +
            "applicationDate=1970-01-01, cancelDate=+999999999-12-31, editedDate=+999999999-12-31, remindDate=+999999999-12-31, " +
            "status=ALLOWED, teamInformed=true, hours=PT10H}");
    }

    @Test
    void ensureGetOvertimeReductionSharesForSingleDays() {
        final LocalDate date = LocalDate.of(2022, 8, 10);

        final Application application = new Application();
        application.setStartDate(date);
        application.setEndDate(date);
        application.setStatus(WAITING);
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        application.setHours(Duration.ofHours(3).plusMinutes(40));

        final Map<LocalDate, Duration> partitionedDurations = application.getOvertimeReductionShares();
        assertThat(partitionedDurations).containsExactlyInAnyOrderEntriesOf(Map.of(
            date, Duration.ofHours(3).plusMinutes(40))
        );
    }

    @Test
    void ensureGetOvertimeReductionSharesForMultipleDays() {
        final LocalDate startDate = LocalDate.of(2022, 8, 10);
        final LocalDate middleDate = LocalDate.of(2022, 8, 11);
        final LocalDate endDate = LocalDate.of(2022, 8, 12);

        final Application application = new Application();
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setStatus(WAITING);
        application.setVacationType(createVacationType(1L, OVERTIME, new StaticMessageSource()));
        application.setHours(Duration.ofHours(12));

        final var partitionedDurations = application.getOvertimeReductionShares();
        assertThat(partitionedDurations).containsExactlyInAnyOrderEntriesOf(Map.of(
            startDate, ofHours(4),
            middleDate, ofHours(4),
            endDate, ofHours(4))
        );
    }

    @Test
    void equals() {
        final Application applicationOne = new Application();
        applicationOne.setId(1L);

        final Application applicationOneOne = new Application();
        applicationOneOne.setId(1L);

        final Application applicationTwo = new Application();
        applicationTwo.setId(2L);

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
        applicationOne.setId(1L);

        assertThat(applicationOne.hashCode()).isEqualTo(32);
    }
}
