package org.synyx.urlaubsverwaltung.web;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FilterPeriodTest {

    @Test
    void ensureThrowsIfInitializedWithNullStartDateString() {

        final LocalDate endDate = LocalDate.of(2199, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(null, "19.05.2199");
        assertThat(filterPeriod.getStartDate()).isInstanceOf(LocalDate.class);
        assertThat(filterPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureThrowsIfInitializedWithNullEndDateString() {

        final LocalDate now = LocalDate.now(Clock.systemUTC());
        final LocalDate startDate = LocalDate.of(2015, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod("19.05.2015", null);
        assertThat(filterPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(filterPeriod.getEndDate()).isEqualTo(LocalDate.of(now.getYear(), DECEMBER, 31));
    }

    @Test
    void ensureThrowsIfInitializedWithEmptyStringStartDateString() {

        final LocalDate now = LocalDate.now(Clock.systemUTC());
        final LocalDate endDate = LocalDate.of(2199, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod("", "19.05.2199");
        assertThat(filterPeriod.getStartDate()).isEqualTo(LocalDate.of(now.getYear(), JANUARY, 1));
        assertThat(filterPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureThrowsIfInitializedWithEmptyStringEndDateString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod("19.05.2015", "");
        assertThat(filterPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(filterPeriod.getEndDate()).isInstanceOf(LocalDate.class);
    }

    @Test
    void ensureDatesCanBeParsedFromString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);
        final LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);

        final FilterPeriod period = new FilterPeriod("19.05.2015", "21.12.2015");
        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureStartDateSetterIsOk() {

        final FilterPeriod filterPeriod = new FilterPeriod("", "19.05.2199");
        filterPeriod.setStartDate(null);

        assertThat(filterPeriod.getStartDateAsString()).isEmpty();
        assertThat(filterPeriod.getEndDateAsString()).isEqualTo("19.05.2199");
    }

    @Test
    void ensureEndDateSetterIsOk() {

        final FilterPeriod filterPeriod = new FilterPeriod("19.05.1899", "");
        filterPeriod.setEndDate(null);

        assertThat(filterPeriod.getStartDateAsString()).isEqualTo("19.05.1899");
        assertThat(filterPeriod.getEndDateAsString()).isEmpty();
    }

    @Test
    void ensureThrowsIfTryingToParseDatesWithUnsupportedFormat() {

        Consumer<String> assertDateFormatNotSupported = (dateString) -> {
            try {
                new FilterPeriod(dateString, dateString);
                Assert.fail("Should throw for date string = " + dateString);
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertDateFormatNotSupported.accept("2015-12-13");
        assertDateFormatNotSupported.accept("12/13/2015");
        assertDateFormatNotSupported.accept("13-12-2015");
    }
}
