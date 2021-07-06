package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.springframework.validation.Errors;

public class WorkTimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";

    private WorkTimeSettingsValidator() {
        // private
    }

    public static void validateWorkingTimeSettings(WorkingTimeSettingsEmbeddable workingTimeSettingsEmbeddable, Errors errors) {

        if (workingTimeSettingsEmbeddable.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsEmbeddable.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsEmbeddable.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsEmbeddable.getWorkingDays() == null || workingTimeSettingsEmbeddable.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingTimeSettings.workingDays", ERROR_MANDATORY_FIELD);
        }
    }

    public static void validateWorkingTimeSettings(WorkingTimeSettingsDto workingTimeSettingsDto, Errors errors) {

        if (workingTimeSettingsDto.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettingsDto.getWorkingDays() == null || workingTimeSettingsDto.getWorkingDays().isEmpty()) {
            errors.rejectValue("workingTimeSettings.workingDays", ERROR_MANDATORY_FIELD);
        }
    }
}
