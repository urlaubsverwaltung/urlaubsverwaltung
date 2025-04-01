package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.validation.Errors;

public class WorkingTimeSettingsValidator {

    private static final String ERROR_WORKING_TIME_MANDATORY = "settings.workingTime.error.mandatory";

    private WorkingTimeSettingsValidator() {
        // private
    }

    public static void validateWorkingTimeSettings(WorkingTimeSettings workingTimeSettings, Errors errors) {

        if (workingTimeSettings.getWorkingDays() == null || workingTimeSettings.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingTimeSettings.workingDays", ERROR_WORKING_TIME_MANDATORY);
        }
    }
}
