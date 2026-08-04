package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;

import static java.time.Month.APRIL;
import static java.time.Month.FEBRUARY;
import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsTest {

    @Test
    void ensureDefaultValues() {

        final AccountSettings settings = new AccountSettings();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
        assertThat(settings.getExpiryDateDayOfMonth()).isEqualTo(1);
        assertThat(settings.getExpiryDateMonth()).isEqualTo(APRIL);
    }

    @Test
    void ensureExpiryDateForYear() {

        final AccountSettings sut = new AccountSettings();
        sut.setExpiryDateDayOfMonth(31);
        sut.setExpiryDateMonth(FEBRUARY);

        // 2024 is a leap year
        assertThat(sut.getExpiryDateForYear(Year.of(2023))).isEqualTo(LocalDate.of(2023, FEBRUARY, 28));
        assertThat(sut.getExpiryDateForYear(Year.of(2024))).isEqualTo(LocalDate.of(2024, FEBRUARY, 29));
        assertThat(sut.getExpiryDateForYear(Year.of(2025))).isEqualTo(LocalDate.of(2025, FEBRUARY, 28));
        assertThat(sut.getExpiryDateForYear(Year.of(2026))).isEqualTo(LocalDate.of(2026, FEBRUARY, 28));
    }
}
