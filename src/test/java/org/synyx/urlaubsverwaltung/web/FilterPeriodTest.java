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
        assertThat(filterPeriod.getStartDate()).isNull();
        assertThat(filterPeriod.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void ensureThrowsIfInitializedWithNullEndDateString() {

        final LocalDate startDate = LocalDate.of(2015, MAY, 19);

        final FilterPeriod filterPeriod = new FilterPeriod(startDate, null);
        assertThat(filterPeriod.getStartDate()).isEqualTo(startDate);
        assertThat(filterPeriod.getEndDate()).isNull();
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
