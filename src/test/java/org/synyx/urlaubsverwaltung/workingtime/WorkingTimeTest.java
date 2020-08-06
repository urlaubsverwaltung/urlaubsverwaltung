package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;
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
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

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
        workingTime.setWorkingDays(workingDays, DayLength.FULL);

        boolean returnValue = workingTime.hasWorkingDays(workingDaysToCompare);
        assertThat(returnValue).isFalse();
    }
}
