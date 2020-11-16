package org.synyx.urlaubsverwaltung.account;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.settings.Settings;

public class AccountSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private static final int DAYS_PER_YEAR = 366;

    private AccountSettingsValidator(){
        // private
    }

    public static void validateAccountSettings(Settings settings, Errors errors) {

        final AccountSettings accountSettings = settings.getAccountSettings();

        final Integer maximumAnnualVacationDays = accountSettings.getMaximumAnnualVacationDays();
        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }
    }
}
