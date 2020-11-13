package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getFirstDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateUtil.getLastDayOfYear;

class FilterPeriodTest {

    @Test
    void ensureThrowsIfInitializedWithNullStartDateString() {

        final LocalDate endDate = LocalDate.of(2199, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(null, endDate);
        assertThat(filterPeriod.getStartDate()).isInstanceOf(LocalDate.class);
        assertThat(filterPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureThrowsIfInitializedWithNullEndDateString() {

        final LocalDate now = LocalDate.now(Clock.systemUTC());
        final LocalDate startDate = LocalDate.of(2015, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(startDate, null);
        assertThat(filterPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(filterPeriod.getEndDate()).isEqualTo(LocalDate.of(now.getYear(), DECEMBER, 31));
    }

    @Test
    void ensureDatesCanBeParsedFromString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);
        final LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        assertThat(period.getStartDate()).isEqualTo(startDate);
        assertThat(period.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureStartDateSetterIsOk() {

        final LocalDate now = LocalDate.now(Clock.systemUTC());
        final LocalDate firstDayOfYear = getFirstDayOfYear(now.getYear());

        final FilterPeriod filterPeriod = new FilterPeriod(null, LocalDate.of(2199, MAY, 19));

        assertThat(filterPeriod.getStartDateIsoValue()).isEqualTo(firstDayOfYear.toString());
        assertThat(filterPeriod.getEndDateIsoValue()).isEqualTo("2199-05-19");
    }

    @Test
    void ensureEndDateSetterIsOk() {

        final LocalDate now = LocalDate.now(Clock.systemUTC());
        final LocalDate lastDayOfYear = getLastDayOfYear(now.getYear());

        final FilterPeriod filterPeriod = new FilterPeriod(LocalDate.of(1899, MAY, 19), null);

        assertThat(filterPeriod.getStartDateIsoValue()).isEqualTo("1899-05-19");
        assertThat(filterPeriod.getEndDateIsoValue()).isEqualTo(lastDayOfYear.toString());
    }
}
