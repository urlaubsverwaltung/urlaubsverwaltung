package org.synyx.urlaubsverwaltung.account.settings;

import org.springframework.validation.Errors;

public class AccountSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_DEFAULT_DAYS_SMALLER_OR_EQUAL_THAN_MAX_DAYS = "settings.account.error.defaultMustBeSmallerOrEqualThanMax";

    private static final int DAYS_PER_YEAR = 366;

    private AccountSettingsValidator() {
        // private
    }

    public static Errors validateAccountSettings(AccountSettingsDto accountSettings, Errors errors) {

        final Integer maximumAnnualVacationDays = accountSettings.getMaximumAnnualVacationDays();
        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }

        final Integer defaultVacationDays = accountSettings.getDefaultVacationDays();
        if (defaultVacationDays == null) {
            errors.rejectValue("defaultVacationDays", ERROR_MANDATORY_FIELD);
        } else if (defaultVacationDays < 0 || defaultVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("defaultVacationDays", ERROR_INVALID_ENTRY);
        } else if (maximumAnnualVacationDays != null && defaultVacationDays > maximumAnnualVacationDays) {
            errors.rejectValue("defaultVacationDays", ERROR_DEFAULT_DAYS_SMALLER_OR_EQUAL_THAN_MAX_DAYS);
        }

        return errors;
    }
}
