package org.synyx.urlaubsverwaltung.period;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class WeekDayTest {

    @Test
    void ensureGetByDayOfWeekThrowsForInvalidNumber() {

        Consumer<Integer> assertValidDayOfWeek = (dayOfWeek) -> {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> WeekDay.getByDayOfWeek(dayOfWeek));
        };

        assertValidDayOfWeek.accept(-1);
        assertValidDayOfWeek.accept(0);
        assertValidDayOfWeek.accept(8);
    }


    @Test
    void ensureGetByDayOfWeekReturnsCorrectWeekDay() {

        WeekDay weekDay = WeekDay.getByDayOfWeek(DayOfWeek.MONDAY.getValue());

        assertThat(weekDay).isEqualTo(WeekDay.MONDAY);
    }
}
