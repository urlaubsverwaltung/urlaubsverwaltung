package org.synyx.urlaubsverwaltung.application.settings;

import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;

public class ApplicationSettingsValidator {

    private ApplicationSettingsValidator() {
        // private
    }

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    public static Errors validateApplicationSettings(ApplicationSettingsDto applicationSettings, Errors errors) {

        final Integer maximumMonthsToApplyForLeaveInAdvance = applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance();
        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForWaitingApplications = applicationSettings.getDaysBeforeRemindForWaitingApplications();
        if (daysBeforeRemindForWaitingApplications == null) {
            errors.rejectValue("daysBeforeRemindForWaitingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForWaitingApplications <= 0) {
            errors.rejectValue("daysBeforeRemindForWaitingApplications", ERROR_INVALID_ENTRY);
        }

        return errors;
    }
}
