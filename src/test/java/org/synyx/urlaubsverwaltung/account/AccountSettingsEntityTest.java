package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.settings.AccountSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsEntityTest {

    @Test
    void ensureDefaultValues() {

        final AccountSettingsEntity settings = new AccountSettingsEntity();
        assertThat(settings.getMaximumAnnualVacationDays()).isEqualTo(40);
    }
}
