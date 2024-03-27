package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import java.time.Month;
import java.time.MonthDay;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsTest {

    @Test
    void ensureDefaultValues() {

        final AccountSettings settings = new AccountSettings();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
        assertThat(settings.getExpiryDate()).isEqualTo(MonthDay.of(Month.APRIL, 1));
    }
}
