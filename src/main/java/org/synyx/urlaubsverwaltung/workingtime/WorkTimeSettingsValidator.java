package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.settings.Settings;

public class WorkTimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private WorkTimeSettingsValidator(){
        // private
    }

    public static void validatePublicHolidaysSettings(Settings settings, Errors errors) {

        final WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        if (workingTimeSettings.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }
}
