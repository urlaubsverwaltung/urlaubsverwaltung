package org.synyx.urlaubsverwaltung.absence;

import org.springframework.validation.Errors;

public class TimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final int HOURS_PER_DAY = 24;

    private TimeSettingsValidator() {
        // private
    }

    public static void validateTimeSettings(TimeSettings timeSettings, Errors errors) {

        final String workDayBeginHourAttribute = "timeSettings.workDayBeginHour";
        final Integer workDayBeginHour = timeSettings.getWorkDayBeginHour();
        validateWorkDayHour(workDayBeginHour, workDayBeginHourAttribute, errors);

        final Integer workDayEndHour = timeSettings.getWorkDayEndHour();
        final String workDayEndHourAttribute = "timeSettings.workDayEndHour";
        validateWorkDayHour(workDayEndHour, workDayEndHourAttribute, errors);

        final boolean beginHourValid = workDayBeginHour != null && isValidWorkDayHour(workDayBeginHour);
        final boolean endHourValid = workDayEndHour != null && isValidWorkDayHour(workDayEndHour);
        final boolean beginAndEndValid = beginHourValid && endHourValid;

        if (beginAndEndValid && workDayBeginHour >= workDayEndHour) {
            errors.rejectValue(workDayBeginHourAttribute, ERROR_INVALID_ENTRY);
            errors.rejectValue(workDayEndHourAttribute, ERROR_INVALID_ENTRY);
        }
    }

    private static void validateWorkDayHour(Integer workDayHour, String attribute, Errors errors) {

        if (workDayHour == null) {
            errors.rejectValue(attribute, ERROR_MANDATORY_FIELD);
        } else {
            if (!isValidWorkDayHour(workDayHour)) {
                errors.rejectValue(attribute, ERROR_INVALID_ENTRY);
            }
        }
    }

    private static boolean isValidWorkDayHour(int workDayHour) {
        return workDayHour > 0 && workDayHour <= HOURS_PER_DAY;
    }
}
