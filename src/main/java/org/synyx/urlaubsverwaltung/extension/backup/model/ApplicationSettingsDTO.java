package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;

public record ApplicationSettingsDTO(
    Integer maximumMonthsToApplyForLeaveInAdvance,
    Integer maximumMonthsToApplyForLeaveAfterwards,
    boolean remindForWaitingApplications,
    boolean allowHalfDays,
    Integer daysBeforeRemindForWaitingApplications,
    boolean remindForUpcomingApplications,
    Integer daysBeforeRemindForUpcomingApplications,
    boolean remindForUpcomingHolidayReplacement,
    Integer daysBeforeRemindForUpcomingHolidayReplacement
) {

    public static ApplicationSettingsDTO of(ApplicationSettings applicationSettings) {
        return new ApplicationSettingsDTO(
            applicationSettings.getMaximumMonthsToApplyForLeaveInAdvance(),
            applicationSettings.getMaximumMonthsToApplyForLeaveAfterwards(),
            applicationSettings.isRemindForWaitingApplications(),
            applicationSettings.isAllowHalfDays(),
            applicationSettings.getDaysBeforeRemindForWaitingApplications(),
            applicationSettings.isRemindForUpcomingApplications(),
            applicationSettings.getDaysBeforeRemindForUpcomingApplications(),
            applicationSettings.isRemindForUpcomingHolidayReplacement(),
            applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement()
        );
    }

    public ApplicationSettings toApplicationSettings() {
        ApplicationSettings applicationSettings = new ApplicationSettings();
        applicationSettings.setMaximumMonthsToApplyForLeaveInAdvance(maximumMonthsToApplyForLeaveInAdvance);
        applicationSettings.setMaximumMonthsToApplyForLeaveAfterwards(maximumMonthsToApplyForLeaveAfterwards);
        applicationSettings.setRemindForWaitingApplications(remindForWaitingApplications);
        applicationSettings.setAllowHalfDays(allowHalfDays);
        applicationSettings.setDaysBeforeRemindForWaitingApplications(daysBeforeRemindForWaitingApplications);
        applicationSettings.setRemindForUpcomingApplications(remindForUpcomingApplications);
        applicationSettings.setDaysBeforeRemindForUpcomingApplications(daysBeforeRemindForUpcomingApplications);
        applicationSettings.setRemindForUpcomingHolidayReplacement(remindForUpcomingHolidayReplacement);
        applicationSettings.setDaysBeforeRemindForUpcomingHolidayReplacement(daysBeforeRemindForUpcomingHolidayReplacement);
        return applicationSettings;
    }
}
