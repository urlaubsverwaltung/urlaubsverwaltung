package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.WeekDay;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.ZERO;


/**
 * Unit test for {@link WorkingTime}.
 */
class WorkingTimeTest {

    @Test
    void testDefaultValues() {

        WorkingTime workingTime = new WorkingTime();

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
    void testHasWorkingDaysIdentical() {

        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());

        List<Integer> workingDaysToCompare = Arrays.asList(FRIDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), MONDAY.getValue(), THURSDAY.getValue());

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);
        assertThat(returnValue).isTrue();
    }


    @Test
    void testHasWorkingDaysDifferent() {

        List<Integer> workingDays = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), FRIDAY.getValue());

        List<Integer> workingDaysToCompare = Arrays.asList(MONDAY.getValue(), TUESDAY.getValue(),
            WEDNESDAY.getValue(), THURSDAY.getValue(), SUNDAY.getValue());

        WorkingTime workingTime = new WorkingTime();
        workingTime.setWorkingDays(workingDays, FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);
        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureWorkingDaysInWorkingTimeList() {

        List<Integer> workingDays = Stream.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .map(DayOfWeek::getValue).collect(Collectors.toList());

        WorkingTime workingEveryDay = new WorkingTime();
        workingEveryDay.setWorkingDays(workingDays, FULL);

        assertThat(workingEveryDay.getWorkingDays())
            .containsExactly(WeekDay.MONDAY, WeekDay.TUESDAY, WeekDay.WEDNESDAY, WeekDay.THURSDAY, WeekDay.FRIDAY, WeekDay.SATURDAY, WeekDay.SUNDAY);

        WorkingTime workingNoDay = new WorkingTime();
        workingNoDay.setWorkingDays(workingDays, ZERO);

        assertThat(workingNoDay.getWorkingDays()).isEmpty();
    }
}
