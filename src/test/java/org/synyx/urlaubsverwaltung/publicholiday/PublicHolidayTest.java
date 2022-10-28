package org.synyx.urlaubsverwaltung.publicholiday;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PublicHolidayTest {

    @Test
    void getWorkingDurationFull() {

        final PublicHoliday publicHoliday = new PublicHoliday(LocalDate.MIN, DayLength.FULL, "description");
        assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getWorkingDurationMorning() {

        final PublicHoliday publicHoliday = new PublicHoliday(LocalDate.MIN, DayLength.MORNING, "description");
        assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkingDurationNoon() {

        final PublicHoliday publicHoliday = new PublicHoliday(LocalDate.MIN, DayLength.NOON, "description");
        assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
    }

    @Test
    void getWorkingDurationZero() {

        final PublicHoliday publicHoliday = new PublicHoliday(LocalDate.MIN, DayLength.ZERO, "description");
        assertThat(publicHoliday.getWorkingDuration()).isEqualByComparingTo(BigDecimal.ONE);
    }
}
