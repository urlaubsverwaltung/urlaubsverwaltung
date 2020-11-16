package org.synyx.urlaubsverwaltung.application;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.settings.Settings;

public class ApplicationSettingsValidator {

    private ApplicationSettingsValidator() {
        // private
    }

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    public static void validateApplicationSettings(Settings settings, Errors errors) {

        final ApplicationSettings applicationSettings = settings.getApplicationSettings();

        final Integer maximumMonthsToApplyForLeaveInAdvance = applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance();
        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForWaitingApplications = applicationSettings.getDaysBeforeRemindForWaitingApplications();
        if (daysBeforeRemindForWaitingApplications == null) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForWaitingApplications <= 0) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", ERROR_INVALID_ENTRY);
        }
    }
}
