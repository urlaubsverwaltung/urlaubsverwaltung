package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.validation.Errors;

public class WorkTimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_WORKING_TIME_MANDATORY = "settings.workingTime.error.mandatory";

    private WorkTimeSettingsValidator() {
        // private
    }

    public static void validateWorkingTimeSettings(WorkingTimeSettings workingTimeSettings, Errors errors) {

        if (workingTimeSettings.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDays() == null || workingTimeSettings.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingTimeSettings.workingDays", ERROR_WORKING_TIME_MANDATORY);
        }
    }
}
