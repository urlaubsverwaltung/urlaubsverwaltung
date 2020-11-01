package org.synyx.urlaubsverwaltung.period;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class PeriodTest {

    @Test
    void ensureThrowsOnNullStartDate() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(null, LocalDate.now(UTC), DayLength.FULL));
    }

    @Test
    void ensureThrowsOnNullEndDate() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(LocalDate.now(UTC), null, DayLength.FULL));
    }


    @Test
    void ensureThrowsOnNullDayLength() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(LocalDate.now(UTC), LocalDate.now(UTC), null));
    }


    @Test
    void ensureThrowsOnZeroDayLength() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(LocalDate.now(UTC), LocalDate.now(UTC), DayLength.ZERO));
    }


    @Test
    void ensureThrowsIfEndDateIsBeforeStartDate() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDateBeforeStartDate = startDate.minusDays(1);

        assertThatIllegalArgumentException().isThrownBy(() -> new Period(startDate, endDateBeforeStartDate, DayLength.FULL));
    }


    @Test
    void ensureThrowsIfStartAndEndDateAreNotSameForMorningDayLength() {
        LocalDate startDate = LocalDate.now(UTC);
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(startDate, startDate.plusDays(1), DayLength.MORNING));
    }


    @Test
    void ensureThrowsIfStartAndEndDateAreNotSameForNoonDayLength() {
        LocalDate startDate = LocalDate.now(UTC);
        assertThatIllegalArgumentException().isThrownBy(() -> new Period(startDate, startDate.plusDays(1), DayLength.NOON));
    }


    @Test
    void ensureCanBeInitializedWithFullDay() {

        LocalDate startDate = LocalDate.now(UTC);
        LocalDate endDate = startDate.plusDays(1);

        Period period = new Period(startDate, endDate, DayLength.FULL);

        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
        assertThat(period.getDayLength()).isEqualTo(DayLength.FULL);
    }


    @Test
    void ensureCanBeInitializedWithHalfDay() {

        LocalDate date = LocalDate.now(UTC);

        Period period = new Period(date, date, DayLength.MORNING);

        assertThat(period.getStartDate()).isEqualTo(date);
        assertThat(period.getEndDate()).isEqualTo(date);
        assertThat(period.getDayLength()).isEqualTo(DayLength.MORNING);
    }
}
