package org.synyx.urlaubsverwaltung.period;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.function.Consumer;


class WeekDayTest {

    @Test
    void ensureGetByDayOfWeekThrowsForInvalidNumber() {

        Consumer<Integer> assertValidDayOfWeek = (dayOfWeek) -> {
            try {
                WeekDay.getByDayOfWeek(dayOfWeek);
                Assert.fail("Should throw for: " + dayOfWeek);
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertValidDayOfWeek.accept(-1);
        assertValidDayOfWeek.accept(0);
        assertValidDayOfWeek.accept(8);
    }


    @Test
    void ensureGetByDayOfWeekReturnsCorrectWeekDay() {

        WeekDay weekDay = WeekDay.getByDayOfWeek(DayOfWeek.MONDAY.getValue());

        Assert.assertNotNull("Missing week day", weekDay);
        Assert.assertEquals("Wrong week day", WeekDay.MONDAY, weekDay);
    }
}
