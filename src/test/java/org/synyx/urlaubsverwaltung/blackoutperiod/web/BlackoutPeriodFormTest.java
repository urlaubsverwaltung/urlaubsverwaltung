package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutPeriodFormTest {

    @Test
    void ensureDefaults() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();

        assertThat(form.getId()).isNull();
        assertThat(form.getTitle()).isNull();
        assertThat(form.getStartDate()).isNull();
        assertThat(form.getEndDate()).isNull();
        assertThat(form.getDepartmentIds()).isEmpty();
        assertThat(form.getVacationTypeIds()).isEmpty();
    }

    @Test
    void ensurePropertiesCanBeSetAndRead() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setId(1L);
        form.setTitle("Jahresabschluss");
        form.setStartDate(LocalDate.of(2026, 12, 20));
        form.setEndDate(LocalDate.of(2027, 1, 5));
        form.setDepartmentIds(List.of(42L));
        form.setVacationTypeIds(List.of(1L));

        assertThat(form.getId()).isEqualTo(1L);
        assertThat(form.getTitle()).isEqualTo("Jahresabschluss");
        assertThat(form.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 20));
        assertThat(form.getEndDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(form.getDepartmentIds()).containsExactly(42L);
        assertThat(form.getVacationTypeIds()).containsExactly(1L);
    }

    @Test
    void ensureIsoValuesAreEmptyStringsWhenDatesAreNotSet() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();

        assertThat(form.getStartDateIsoValue()).isEmpty();
        assertThat(form.getEndDateIsoValue()).isEmpty();
    }

    @Test
    void ensureIsoValuesAreFormattedDates() {

        final BlackoutPeriodForm form = new BlackoutPeriodForm();
        form.setStartDate(LocalDate.of(2026, 12, 20));
        form.setEndDate(LocalDate.of(2027, 1, 5));

        assertThat(form.getStartDateIsoValue()).isEqualTo("2026-12-20");
        assertThat(form.getEndDateIsoValue()).isEqualTo("2027-01-05");
    }
}
