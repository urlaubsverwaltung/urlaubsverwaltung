package org.synyx.urlaubsverwaltung.sicknote.settings;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteSettings;

public class SickNoteSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private SickNoteSettingsValidator() {
        // private
    }

    public static Errors validateSickNoteSettings(SickNoteSettingsDto sickNoteSettings, Errors errors) {

        final Integer maximumSickPayDays = sickNoteSettings.getMaximumSickPayDays();
        final Integer daysBeforeEndOfSickPayNotification = sickNoteSettings.getDaysBeforeEndOfSickPayNotification();

        if (maximumSickPayDays == null) {
            errors.rejectValue("maximumSickPayDays", ERROR_MANDATORY_FIELD);
        } else if (maximumSickPayDays < 0) {
            errors.rejectValue("maximumSickPayDays", ERROR_INVALID_ENTRY);
        }

        if (daysBeforeEndOfSickPayNotification == null) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeEndOfSickPayNotification < 0) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification", ERROR_INVALID_ENTRY);
        }

        if (maximumSickPayDays != null && daysBeforeEndOfSickPayNotification != null
            && daysBeforeEndOfSickPayNotification > maximumSickPayDays) {
            errors.rejectValue("daysBeforeEndOfSickPayNotification",
                "daysBeforeEndOfSickPayNotification.error");
        }

        return errors;
    }
}
