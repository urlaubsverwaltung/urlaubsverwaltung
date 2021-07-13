package org.synyx.urlaubsverwaltung.overtime.settings;

import org.springframework.validation.Errors;

public class OvertimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private OvertimeSettingsValidator() {
        // private
    }

    public static Errors validateOvertimeSettings(OvertimeSettingsDto overtimeSettings, Errors errors) {

        validateOvertimeLimit(overtimeSettings.getMaximumOvertime(), "maximumOvertime", errors);
        validateOvertimeLimit(overtimeSettings.getMinimumOvertime(), "minimumOvertime", errors);

        final Integer minimumOvertimeReduction = overtimeSettings.getMinimumOvertimeReduction();
        if (minimumOvertimeReduction == null || minimumOvertimeReduction < 0) {
            errors.rejectValue("minimumOvertimeReduction", "settings.overtime.minimumOvertimeReduction.error");
        }

        return errors;
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
