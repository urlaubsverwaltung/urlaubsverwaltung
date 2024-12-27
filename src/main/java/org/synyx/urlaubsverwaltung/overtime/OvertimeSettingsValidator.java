package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.validation.Errors;

public class OvertimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private OvertimeSettingsValidator() {
        // private
    }

    public static void validateOvertimeSettings(OvertimeSettings overtimeSettings, Errors errors) {

        if (!overtimeSettings.isOvertimeActive()) {
            return;
        }

        validateOvertimeLimit(overtimeSettings.getMaximumOvertime(), "overtimeSettings.maximumOvertime", errors);
        validateOvertimeLimit(overtimeSettings.getMinimumOvertime(), "overtimeSettings.minimumOvertime", errors);

        final Integer minimumOvertimeReduction = overtimeSettings.getMinimumOvertimeReduction();
        if (minimumOvertimeReduction == null || minimumOvertimeReduction < 0) {
            errors.rejectValue("overtimeSettings.minimumOvertimeReduction", "settings.overtime.minimumOvertimeReduction.error");
        }
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
