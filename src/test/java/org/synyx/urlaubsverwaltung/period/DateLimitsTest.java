package org.synyx.urlaubsverwaltung.period;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.absence.DateRange;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.OCTOBER;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateLimitsTest {

    // 2026-10-12
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-12T00:00:00.00Z"), UTC);

    @Test
    void ensureThreeCalendarYearsAreNotTooLong() {
        final LocalDate startDate = LocalDate.of(2027, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2029, DECEMBER, 31);
        assertThat(DateLimits.exceedsMaximumRequestedDuration(startDate, endDate)).isFalse();
    }

    @Test
    void ensureThreeCalendarYearsIncludingLeapDayAreNotTooLong() {
        final LocalDate startDate = LocalDate.of(2028, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2030, DECEMBER, 31);
        assertThat(DateLimits.exceedsMaximumRequestedDuration(startDate, endDate)).isFalse();
    }

    @Test
    void ensureMoreThanThreeCalendarYearsAreTooLong() {
        final LocalDate startDate = LocalDate.of(2027, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2030, JANUARY, 31);
        assertThat(DateLimits.exceedsMaximumRequestedDuration(startDate, endDate)).isTrue();
    }

    @Test
    void ensureHugeDateRangeIsTooLong() {
        final LocalDate startDate = LocalDate.of(2026, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(999_999_998, DECEMBER, 31);
        assertThat(DateLimits.exceedsMaximumRequestedDuration(startDate, endDate)).isTrue();
    }

    @Test
    void ensureSingleDayIsNotTooLong() {
        final LocalDate date = LocalDate.of(2026, JANUARY, 1);
        assertThat(DateLimits.exceedsMaximumRequestedDuration(date, date)).isFalse();
    }

    @Test
    void ensureTwoYearsInTheFutureAreNotTooFar() {
        assertThat(DateLimits.isTooFarInTheFuture(LocalDate.of(2028, OCTOBER, 12), clock)).isFalse();
    }

    @Test
    void ensureMoreThanTwoYearsInTheFutureAreTooFar() {
        assertThat(DateLimits.isTooFarInTheFuture(LocalDate.of(2028, OCTOBER, 13), clock)).isTrue();
        assertThat(DateLimits.isTooFarInTheFuture(LocalDate.MAX, clock)).isTrue();
    }

    @Test
    void ensureTenYearsInThePastAreNotTooFar() {
        assertThat(DateLimits.isTooFarInThePast(LocalDate.of(2016, OCTOBER, 12), clock)).isFalse();
    }

    @Test
    void ensureMoreThanTenYearsInThePastAreTooFar() {
        assertThat(DateLimits.isTooFarInThePast(LocalDate.of(2016, OCTOBER, 11), clock)).isTrue();
        assertThat(DateLimits.isTooFarInThePast(LocalDate.MIN, clock)).isTrue();
    }

    @Test
    void ensureHundredYearsAreCalculable() {
        final DateRange dateRange = new DateRange(LocalDate.of(1926, OCTOBER, 12), LocalDate.of(2026, OCTOBER, 12));
        assertThatCode(() -> DateLimits.requireCalculableDuration(dateRange)).doesNotThrowAnyException();
    }

    @Test
    void ensureMoreThanHundredYearsIncludingLeapDaysAreNotCalculable() {
        final LocalDate startDate = LocalDate.of(1926, OCTOBER, 12);
        final DateRange dateRange = new DateRange(startDate, startDate.plusDays(100 * 366L));
        assertThatThrownBy(() -> DateLimits.requireCalculableDuration(dateRange))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
