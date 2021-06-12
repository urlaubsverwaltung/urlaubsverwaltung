package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


/**
 * Unit test for {@link WorkingTime}.
 */
class WorkingTimeTest {

    @Test
    void testDefaultValues() {

        final Person person = new Person();
        person.setId(1);

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN);
        assertThat(workingTime.getMonday()).isEqualTo(ZERO);
        assertThat(workingTime.getTuesday()).isEqualTo(ZERO);
        assertThat(workingTime.getWednesday()).isEqualTo(ZERO);
        assertThat(workingTime.getThursday()).isEqualTo(ZERO);
        assertThat(workingTime.getFriday()).isEqualTo(ZERO);
        assertThat(workingTime.getSaturday()).isEqualTo(ZERO);
        assertThat(workingTime.getSunday()).isEqualTo(ZERO);
        assertThat(workingTime.getFederalStateOverride()).isEmpty();
    }

    @Test
    void ensureWorkingDaysInWorkingTimeList() {

        final Person person = new Person();
        person.setId(1);

        final List<DayOfWeek> workingDays = List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY);

        final WorkingTime workingEveryDay = new WorkingTime(person, LocalDate.MIN);
        workingEveryDay.setWorkingDays(workingDays, FULL);

        assertThat(workingEveryDay.getWorkingDays())
            .containsExactly(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY);

        final WorkingTime workingNoDay = new WorkingTime(person, LocalDate.MIN);
        workingNoDay.setWorkingDays(workingDays, ZERO);

        assertThat(workingNoDay.getWorkingDays()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "MONDAY,FULL", "MONDAY,MORNING", "MONDAY,NOON",
        "TUESDAY,FULL", "TUESDAY,MORNING", "TUESDAY,NOON",
        "WEDNESDAY,FULL", "WEDNESDAY,MORNING", "WEDNESDAY,NOON",
        "THURSDAY,FULL", "THURSDAY,MORNING", "THURSDAY,NOON",
        "FRIDAY,FULL", "FRIDAY,MORNING", "FRIDAY,NOON",
        "SATURDAY,FULL", "SATURDAY,MORNING", "SATURDAY,NOON",
        "SUNDAY,FULL", "SUNDAY,MORNING", "SUNDAY,NOON"
    })
    void ensureIsWorkingDayIsTrue(DayOfWeek dayOfWeek, DayLength dayLength) {
        final Person person = new Person();
        person.setId(1);

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN);
        workingTime.setDayLengthForWeekDay(dayOfWeek, dayLength);
        assertThat(workingTime.isWorkingDay(dayOfWeek)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek.class)
    void ensureIsWorkingDayIsFalseForDayLengthZero(DayOfWeek dayOfWeek) {
        final Person person = new Person();
        person.setId(1);

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.MIN);
        workingTime.setDayLengthForWeekDay(dayOfWeek, ZERO);
        assertThat(workingTime.isWorkingDay(dayOfWeek)).isFalse();
    }

    @Test
    void ensureEqualsConsidersPerson() {
        final Person batman = new Person();
        batman.setId(1);

        final Person robin = new Person();
        robin.setId(2);

        final WorkingTime workingTimeBatman = new WorkingTime(batman, LocalDate.of(2021, JUNE, 12));
        final WorkingTime workingTimeRobin = new WorkingTime(robin, LocalDate.of(2021, JUNE, 12));

        assertThat(workingTimeBatman)
            .isEqualTo(workingTimeBatman)
            .isNotEqualTo(workingTimeRobin);
    }

    @Test
    void ensureEqualsConsidersValidFrom() {
        final Person batman = new Person();
        batman.setId(1);

        final WorkingTime workingTimeOne = new WorkingTime(batman, LocalDate.of(2021, JUNE, 12));
        final WorkingTime workingTimeTwo = new WorkingTime(batman, LocalDate.of(2021, JUNE, 13));

        assertThat(workingTimeOne)
            .isEqualTo(workingTimeOne)
            .isNotEqualTo(workingTimeTwo);
    }

    @Test
    void hashCodeTest() {
        final Person person = new Person();
        person.setId(1);

        final WorkingTime workingTime = new WorkingTime(person, LocalDate.of(2021, JUNE, 12));

        assertThat(workingTime.hashCode()).isEqualTo(4141357);
    }
}
