package org.synyx.urlaubsverwaltung.publicholiday;

import org.springframework.validation.Errors;

public class PublicHolidaysSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private PublicHolidaysSettingsValidator() {
        // private
    }

    public static void validatePublicHolidaysSettings(PublicHolidaysSettings publicHolidaysSettings, Errors errors) {

        if (publicHolidaysSettings.getFederalState() == null) {
            errors.rejectValue("publicHolidaysSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (publicHolidaysSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("publicHolidaysSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (publicHolidaysSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("publicHolidaysSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }
}
