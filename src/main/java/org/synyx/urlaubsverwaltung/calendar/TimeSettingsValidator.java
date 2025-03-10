package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.validation.Errors;

import java.time.LocalTime;

public class TimeSettingsValidator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    private TimeSettingsValidator() {
        // private
    }

    public static void validateTimeSettings(final TimeSettings timeSettings, final Errors errors) {

        // validate begin time
        final Integer workDayBeginHour = timeSettings.getWorkDayBeginHour();
        final String workDayBeginHourAttribute = "timeSettings.workDayBeginHour";
        final boolean beginHourValid = validateWorkDayHour(workDayBeginHour, workDayBeginHourAttribute, errors);

        final Integer workDayBeginMinute = timeSettings.getWorkDayBeginMinute();
        final String workDayBeginMinuteAttribute = "timeSettings.workDayBeginMinute";
        final boolean beginMinuteValid = validateWorkDayMinute(workDayBeginMinute, workDayBeginMinuteAttribute, errors);

        // validate end time
        final Integer workDayEndHour = timeSettings.getWorkDayEndHour();
        final String workDayEndHourAttribute = "timeSettings.workDayEndHour";
        final boolean endHourValid = validateWorkDayHour(workDayEndHour, workDayEndHourAttribute, errors);

        final Integer workDayEndMinute = timeSettings.getWorkDayEndMinute();
        final String workDayEndMinuteAttribute = "timeSettings.workDayEndMinute";
        final boolean endMinuteValid = validateWorkDayMinute(workDayEndMinute, workDayEndMinuteAttribute, errors);

        // validate  if start is before end
        final boolean beginAndEndValid = (beginHourValid && beginMinuteValid) && (endHourValid && endMinuteValid);
        if (beginAndEndValid && !LocalTime.of(workDayBeginHour, workDayBeginMinute).isBefore(LocalTime.of(workDayEndHour, workDayEndMinute))) {
            errors.rejectValue(workDayBeginHourAttribute, ERROR_INVALID_ENTRY);
            errors.rejectValue(workDayEndHourAttribute, ERROR_INVALID_ENTRY);
        }
    }

    private static boolean validateWorkDayHour(final Integer hour, final String attribute, final Errors errors) {
        if (hour == null) {
            errors.rejectValue(attribute, ERROR_MANDATORY_FIELD);
        } else if (!isValidWorkDayHour(hour)) {
            errors.rejectValue(attribute, ERROR_INVALID_ENTRY);
        }

        return errors.getFieldErrors(attribute).isEmpty();
    }

    private static boolean validateWorkDayMinute(final Integer minute, final String attribute, final Errors errors) {
        if (minute == null) {
            errors.rejectValue(attribute, ERROR_MANDATORY_FIELD);
        } else if (!isValidWorkDayMinute(minute)) {
            errors.rejectValue(attribute, ERROR_INVALID_ENTRY);
        }

        return errors.getFieldErrors(attribute).isEmpty();
    }

    private static boolean isValidWorkDayHour(final int hour) {
        return hour > 0 && hour < HOURS_PER_DAY;
    }

    private static boolean isValidWorkDayMinute(final int minute) {
        return minute >= 0 && minute < MINUTES_PER_HOUR;
    }
}
