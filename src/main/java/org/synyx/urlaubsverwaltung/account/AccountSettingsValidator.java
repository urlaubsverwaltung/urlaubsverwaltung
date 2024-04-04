package org.synyx.urlaubsverwaltung.account;

import org.springframework.validation.Errors;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static java.time.Month.FEBRUARY;

public class AccountSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_DEFAULT_DAYS_SMALLER_OR_EQUAL_THAN_MAX_DAYS = "settings.account.error.defaultMustBeSmallerOrEqualThanMax";

    private static final int DAYS_PER_YEAR = 366;

    private AccountSettingsValidator() {
        // private
    }

    public static void validateAccountSettings(AccountSettings accountSettings, Errors errors) {

        final Integer maximumAnnualVacationDays = accountSettings.getMaximumAnnualVacationDays();
        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("accountSettings.maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }

        final Integer defaultVacationDays = accountSettings.getDefaultVacationDays();
        if (defaultVacationDays == null) {
            errors.rejectValue("accountSettings.defaultVacationDays", ERROR_MANDATORY_FIELD);
        } else if (defaultVacationDays < 0 || defaultVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("accountSettings.defaultVacationDays", ERROR_INVALID_ENTRY);
        } else if (maximumAnnualVacationDays != null && defaultVacationDays > maximumAnnualVacationDays) {
            errors.rejectValue("accountSettings.defaultVacationDays", ERROR_DEFAULT_DAYS_SMALLER_OR_EQUAL_THAN_MAX_DAYS);
        }

        validateExpiryDateDayOfMonth(errors, accountSettings);
    }

    private static void validateExpiryDateDayOfMonth(Errors errors, AccountSettings accountSettings) {
        final int dayOfMonth = accountSettings.getExpiryDateDayOfMonth();
        final Month month = accountSettings.getExpiryDateMonth();

        if (dayOfMonth < 1
            || dayOfMonth > 31
            || FEBRUARY.equals(month) && dayOfMonth > 29
            || !validDate(Year.now(), month, dayOfMonth)
        ) {
            errors.rejectValue("accountSettings.expiryDateDayOfMonth", ERROR_INVALID_ENTRY);
        }
    }

    @SuppressWarnings("java:S2201")
    private static boolean validDate(Year year, Month month, int dayOfMonth) {
        try {
            LocalDate.of(year.getValue(), month, dayOfMonth);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
