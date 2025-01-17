package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.account.AccountSettings;

import java.time.Month;


public record AccountSettingsDTO(
    Integer defaultVacationDays,
    Integer maximumAnnualVacationDays,
    int expiryDateDayOfMonth,
    Month expiryDateMonth,
    boolean doRemainingVacationDaysExpireGlobally
) {

    public static AccountSettingsDTO of(AccountSettings accountSettings) {
        return new AccountSettingsDTO(
            accountSettings.getDefaultVacationDays(),
            accountSettings.getMaximumAnnualVacationDays(),
            accountSettings.getExpiryDateDayOfMonth(),
            accountSettings.getExpiryDateMonth(),
            accountSettings.isDoRemainingVacationDaysExpireGlobally()
        );
    }

    public AccountSettings toAccountSettings() {
        AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDefaultVacationDays(defaultVacationDays);
        accountSettings.setMaximumAnnualVacationDays(maximumAnnualVacationDays);
        accountSettings.setExpiryDateDayOfMonth(expiryDateDayOfMonth);
        accountSettings.setExpiryDateMonth(expiryDateMonth);
        accountSettings.setDoRemainingVacationDaysExpireGlobally(doRemainingVacationDaysExpireGlobally);
        return accountSettings;
    }
}
