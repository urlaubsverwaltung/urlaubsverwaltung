package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsTest {

    @Test
    void ensureDefaultValues() {

        final AccountSettings settings = new AccountSettings();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
    }
}
