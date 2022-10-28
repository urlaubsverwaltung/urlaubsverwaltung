package org.synyx.urlaubsverwaltung.sicknote.settings;

import org.springframework.validation.Errors;

public class SickNoteSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_ILLEGAL_MAXIMUM_SICKPAYDAYS = "sicknote.error.illegalMaximumSickPayDays";
    private static final int LEGAL_MINIMUM_OF_MAXIMUM_SICK_PAY_DAYS = 42;

    private SickNoteSettingsValidator() {
        // private
    }

    public static void validateSickNoteSettings(SickNoteSettings sickNoteSettings, Errors errors) {

        final Integer maximumSickPayDays = sickNoteSettings.getMaximumSickPayDays();
        final Integer daysBeforeEndOfSickPayNotification = sickNoteSettings.getDaysBeforeEndOfSickPayNotification();

        if (maximumSickPayDays == null) {
            errors.rejectValue("sickNoteSettings.maximumSickPayDays", ERROR_MANDATORY_FIELD);
        } else if (maximumSickPayDays < LEGAL_MINIMUM_OF_MAXIMUM_SICK_PAY_DAYS) {
            errors.rejectValue("sickNoteSettings.maximumSickPayDays", ERROR_ILLEGAL_MAXIMUM_SICKPAYDAYS);
        }

        if (daysBeforeEndOfSickPayNotification == null) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeEndOfSickPayNotification < 0) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification", ERROR_INVALID_ENTRY);
        }

        if (maximumSickPayDays != null && daysBeforeEndOfSickPayNotification != null
            && daysBeforeEndOfSickPayNotification > maximumSickPayDays) {
            errors.rejectValue("sickNoteSettings.daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
        }
    }
}
