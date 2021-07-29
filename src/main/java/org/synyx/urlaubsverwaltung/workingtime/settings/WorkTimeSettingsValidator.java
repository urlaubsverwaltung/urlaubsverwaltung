package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.springframework.validation.Errors;

public class WorkTimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private WorkTimeSettingsValidator() {
        // private
    }

    public static void validateWorkingTimeSettings(WorkingTimeSettingsDto workingTimeSettingsDto, Errors errors) {

        if (workingTimeSettingsDto.getFederalState() == null) {
            errors.rejectValue("federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDays() == null || workingTimeSettingsDto.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingDays", ERROR_MANDATORY_FIELD);
        }
    }
}
