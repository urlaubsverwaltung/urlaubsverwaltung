package org.synyx.urlaubsverwaltung.publicholiday;

import org.springframework.validation.Errors;

public final class PublicHolidaysSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private PublicHolidaysSettingsValidator() {
        // private
    }

    public static void validatePublicHolidaysSettings(PublicHolidaysSettings publicHolidaysSettings, Errors errors) {

        if (publicHolidaysSettings.getFederalState() == null) {
            errors.rejectValue("publicHolidaysSettings.federalState", ERROR_MANDATORY_FIELD);
        }
    }
}
