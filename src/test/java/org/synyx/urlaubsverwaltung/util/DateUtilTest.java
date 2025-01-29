package org.synyx.urlaubsverwaltung.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link DateUtil}.
 */
class DateUtilTest {

    @Test
    void ensureReturnsTrueIfGivenDayIsAWorkDay() {

        // Monday
        LocalDate date = LocalDate.of(2011, 12, 26);

        boolean returnValue = DateUtil.isWorkDay(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseIfGivenDayIsNotAWorkDay() {

        // Sunday
        LocalDate date = LocalDate.of(2014, 11, 23);

        boolean returnValue = DateUtil.isWorkDay(date);

        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureReturnsTrueForChristmasEve() {

        LocalDate date = LocalDate.of(2011, 12, 24);

        boolean returnValue = DateUtil.isChristmasEve(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseForNotChristmasEve() {

        LocalDate date = LocalDate.of(2011, 12, 25);

        boolean returnValue = DateUtil.isChristmasEve(date);

        assertThat(returnValue).isFalse();
    }

    @Test
    void ensureReturnsTrueForNewYearsEve() {

        LocalDate date = LocalDate.of(2014, 12, 31);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        assertThat(returnValue).isTrue();
    }

    @Test
    void ensureReturnsFalseForNotNewYearsEve() {

        LocalDate date = LocalDate.of(2011, 12, 25);

        boolean returnValue = DateUtil.isNewYearsEve(date);

        assertThat(returnValue).isFalse();
    }
}
