package org.synyx.urlaubsverwaltung.application.settings;

import org.springframework.validation.Errors;

public class ApplicationSettingsValidator {

    private ApplicationSettingsValidator() {
        // private
    }

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";

    public static void validateApplicationSettings(ApplicationSettings applicationSettings, Errors errors) {

        final Integer maximumMonthsToApplyForLeaveInAdvance = applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance();
        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }

        final Integer maximumMonthsToApplyForLeaveAfterwards = applicationSettings.getMaximumMonthsToApplyForLeaveAfterwards();
        if (maximumMonthsToApplyForLeaveAfterwards == null) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveAfterwards <= 0) {
            errors.rejectValue("applicationSettings.maximumMonthsToApplyForLeaveAfterwards", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForWaitingApplications = applicationSettings.getDaysBeforeRemindForWaitingApplications();
        if (daysBeforeRemindForWaitingApplications == null) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForWaitingApplications <= 0) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForWaitingApplications", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForUpcomingHolidayReplacement = applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement();
        if (daysBeforeRemindForUpcomingHolidayReplacement == null) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForUpcomingHolidayReplacement <= 0) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement", ERROR_INVALID_ENTRY);
        }

        final Integer daysBeforeRemindForUpcomingApplications = applicationSettings.getDaysBeforeRemindForUpcomingApplications();
        if (daysBeforeRemindForUpcomingApplications == null) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeRemindForUpcomingApplications <= 0) {
            errors.rejectValue("applicationSettings.daysBeforeRemindForUpcomingApplications", ERROR_INVALID_ENTRY);
        }
    }
}
