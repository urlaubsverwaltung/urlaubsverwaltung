package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsTest {

    @Test
    void ensureDefaultValues() {

        final AccountSettings settings = new AccountSettings();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
        assertThat(settings.getExpiryDateDayOfMonth()).isEqualTo(1);
        assertThat(settings.getExpiryDateMonth()).isEqualTo(Month.APRIL);
    }

    @Test
    void ensureExpiryDateForYear() {

        final AccountSettings sut = new AccountSettings();
        sut.setExpiryDateDayOfMonth(31);
        sut.setExpiryDateMonth(Month.FEBRUARY);

        // 2024 is a leap year
        assertThat(sut.getExpiryDateForYear(Year.of(2023))).isEqualTo(LocalDate.of(2023, 2, 28));
        assertThat(sut.getExpiryDateForYear(Year.of(2024))).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(sut.getExpiryDateForYear(Year.of(2025))).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(sut.getExpiryDateForYear(Year.of(2026))).isEqualTo(LocalDate.of(2026, 2, 28));
    }
}
