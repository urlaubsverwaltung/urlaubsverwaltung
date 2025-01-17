package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.account.AccountSettings;

import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSettingsDTOTest {

    @Test
    void happyPathAccountSettingsToDTO() {
        AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(25);
        accountSettings.setMaximumAnnualVacationDays(30);
        accountSettings.setExpiryDateDayOfMonth(31);
        accountSettings.setExpiryDateMonth(Month.DECEMBER);
        accountSettings.setDoRemainingVacationDaysExpireGlobally(true);

        AccountSettingsDTO accountSettingsDTO = AccountSettingsDTO.of(accountSettings);

        assertThat(accountSettingsDTO.defaultVacationDays()).isEqualTo(accountSettings.getDefaultVacationDays());
        assertThat(accountSettingsDTO.maximumAnnualVacationDays()).isEqualTo(accountSettings.getMaximumAnnualVacationDays());
        assertThat(accountSettingsDTO.expiryDateDayOfMonth()).isEqualTo(accountSettings.getExpiryDateDayOfMonth());
        assertThat(accountSettingsDTO.expiryDateMonth()).isEqualTo(accountSettings.getExpiryDateMonth());
        assertThat(accountSettingsDTO.doRemainingVacationDaysExpireGlobally()).isEqualTo(accountSettings.isDoRemainingVacationDaysExpireGlobally());
    }

    @Test
    void happyPathDTOToAccountSettings() {
        AccountSettingsDTO accountSettingsDTO = new AccountSettingsDTO(25, 30, 31, Month.DECEMBER, true);
        AccountSettings accountSettings = accountSettingsDTO.toAccountSettings();

        assertThat(accountSettings.getDefaultVacationDays()).isEqualTo(accountSettingsDTO.defaultVacationDays());
        assertThat(accountSettings.getMaximumAnnualVacationDays()).isEqualTo(accountSettingsDTO.maximumAnnualVacationDays());
        assertThat(accountSettings.getExpiryDateDayOfMonth()).isEqualTo(accountSettingsDTO.expiryDateDayOfMonth());
        assertThat(accountSettings.getExpiryDateMonth()).isEqualTo(accountSettingsDTO.expiryDateMonth());
        assertThat(accountSettings.isDoRemainingVacationDaysExpireGlobally()).isEqualTo(accountSettingsDTO.doRemainingVacationDaysExpireGlobally());
    }
}
