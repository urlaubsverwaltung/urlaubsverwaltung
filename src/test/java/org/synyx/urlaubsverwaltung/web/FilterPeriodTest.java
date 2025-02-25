package org.synyx.urlaubsverwaltung.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.Month.DECEMBER;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;

class FilterPeriodTest {

    @Test
    void ensureThrowsIfInitializedWithNullStartDateString() {

        final LocalDate endDate = LocalDate.of(2199, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(null, endDate);
        assertThat(filterPeriod.startDate()).isNull();
        assertThat(filterPeriod.endDate()).isEqualTo(endDate);
    }

    @Test
    void ensureThrowsIfInitializedWithNullEndDateString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(startDate, null);
        assertThat(filterPeriod.startDate()).isEqualTo(startDate);
        assertThat(filterPeriod.endDate()).isNull();
    }

    @Test
    void ensureDatesCanBeParsedFromString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);
        final LocalDate endDate = LocalDate.of(2015, DECEMBER, 21);
        final FilterPeriod period = new FilterPeriod(startDate, endDate);

        assertThat(period.startDate()).isEqualTo(startDate);
        assertThat(period.endDate()).isEqualTo(endDate);
    }

    @Test
    void ensureStartDateSetterIsOk() {

        final FilterPeriod filterPeriod = new FilterPeriod(null, LocalDate.of(2199, MAY, 19));

        assertThat(filterPeriod.getStartDateIsoValue()).isEmpty();
        assertThat(filterPeriod.getEndDateIsoValue()).isEqualTo("2199-05-19");
    }

    @Test
    void ensureEndDateSetterIsOk() {

        final FilterPeriod filterPeriod = new FilterPeriod(LocalDate.of(1899, MAY, 19), null);

        assertThat(filterPeriod.getStartDateIsoValue()).isEqualTo("1899-05-19");
        assertThat(filterPeriod.getEndDateIsoValue()).isEmpty();
    }
}
