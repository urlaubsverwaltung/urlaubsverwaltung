package org.synyx.urlaubsverwaltung.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link DateUtil}.
 */
class DateUtilTest {

    @Test
    void ensureReturnsTrueIfGivenDayIsAWorkDay() {

        // Monday
        LocalDate date = LocalDate.of(2011, DECEMBER, 26);

        boolean returnValue = DateUtil.isWorkDay(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseIfGivenDayIsNotAWorkDay() {

        // Sunday
        LocalDate date = LocalDate.of(2014, NOVEMBER, 23);

        boolean returnValue = DateUtil.isWorkDay(date);

        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureReturnsTrueIfGivenDayIsOnAWeekend() {

        // Saturday
        LocalDate date = LocalDate.of(2014, NOVEMBER, 22);

        boolean returnValue = DateUtil.isWeekend(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseIfGivenDayIsNotOnAWeekend() {

        // Monday
        LocalDate date = LocalDate.of(2011, DECEMBER, 26);

        boolean returnValue = DateUtil.isWeekend(date);

        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureReturnsTrueForChristmasEve() {

        LocalDate date = LocalDate.of(2011, DECEMBER, 24);

        boolean returnValue = DateUtil.isChristmasEve(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseForNotChristmasEve() {

        LocalDate date = LocalDate.of(2011, DECEMBER, 25);

        boolean returnValue = DateUtil.isChristmasEve(date);

        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureReturnsTrueForNewYearsEve() {

        LocalDate date = LocalDate.of(2014, DECEMBER, 31);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseForNotNewYearsEve() {

        LocalDate date = LocalDate.of(2011, DECEMBER, 25);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        assertThat(returnValue).isFalse();
    }
}
