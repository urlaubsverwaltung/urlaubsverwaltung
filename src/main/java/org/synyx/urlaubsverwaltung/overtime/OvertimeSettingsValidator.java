package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.settings.Settings;

public class OvertimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private OvertimeSettingsValidator() {
        // private
    }

    public static void validateOvertimeSettings(Settings settings, Errors errors) {

        final OvertimeSettings overtimeSettings = settings.getOvertimeSettings();
        if (!overtimeSettings.isOvertimeActive()) {
            return;
        }

        validateOvertimeLimit(overtimeSettings.getMaximumOvertime(), "overtimeSettings.maximumOvertime", errors);
        validateOvertimeLimit(overtimeSettings.getMinimumOvertime(), "overtimeSettings.minimumOvertime", errors);
    }

    private static void validateOvertimeLimit(Integer limit, String field, Errors errors) {

        if (limit == null) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
            return;
        }

        if (limit < 0) {
            errors.rejectValue(field, ERROR_INVALID_ENTRY);
        }
    }
}
