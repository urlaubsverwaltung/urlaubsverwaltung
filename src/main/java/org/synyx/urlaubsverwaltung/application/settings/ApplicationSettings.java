package org.synyx.urlaubsverwaltung.application.settings;

import jakarta.persistence.Embeddable;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class ApplicationSettings {

    /**
     * Specifies how many months in advance a person can apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveInAdvance = 18;

    /**
     * Specifies how many months afterwards can a person apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveAfterwards = 12;

    /**
     * Activates a notification after {daysBeforeWaitingApplicationsReminderNotification} days for waiting applications
     */
    private boolean remindForWaitingApplications = false;

    /**
     * Specifies if applications can be done for half-day leave
     */
    private boolean allowHalfDays = true;

    /**
     * Specifies when a reminder for waiting application should be send to boss / department head /
     * secondary stage authority
     */
    private Integer daysBeforeRemindForWaitingApplications = 2;

    /**
     * Activates a notification {daysBeforeRemindForUpcomingApplications} days before upcoming applications
     */
    private boolean remindForUpcomingApplications = false;

    /**
     * Specifies when a reminder for upcoming application should be sent
     */
    private Integer daysBeforeRemindForUpcomingApplications = 3;

    /**
     * Activates a notification after {daysBeforeRemindForUpcomingHolidayReplacement} days for upcoming replacement
     */
    private boolean remindForUpcomingHolidayReplacement = false;

    /**
     * Specifies when a reminder for upcoming replacement should be sent
     */
    private Integer daysBeforeRemindForUpcomingHolidayReplacement = 3;

    public Integer getMaximumMonthsToApplyForLeaveInAdvance() {
        return maximumMonthsToApplyForLeaveInAdvance;
    }

    public void setMaximumMonthsToApplyForLeaveInAdvance(Integer maximumMonthsToApplyForLeaveInAdvance) {
        this.maximumMonthsToApplyForLeaveInAdvance = maximumMonthsToApplyForLeaveInAdvance;
    }

    public Integer getMaximumMonthsToApplyForLeaveAfterwards() {
        return maximumMonthsToApplyForLeaveAfterwards;
    }

    public void setMaximumMonthsToApplyForLeaveAfterwards(Integer maximumMonthsToApplyForLeaveAfterwards) {
        this.maximumMonthsToApplyForLeaveAfterwards = maximumMonthsToApplyForLeaveAfterwards;
    }

    public boolean isRemindForWaitingApplications() {
        return remindForWaitingApplications;
    }

    public void setRemindForWaitingApplications(boolean remindForWaitingApplications) {
        this.remindForWaitingApplications = remindForWaitingApplications;
    }

    public boolean isAllowHalfDays() {
        return allowHalfDays;
    }

    public void setAllowHalfDays(boolean allowHalfDays) {
        this.allowHalfDays = allowHalfDays;
    }

    public Integer getDaysBeforeRemindForWaitingApplications() {
        return daysBeforeRemindForWaitingApplications;
    }

    public void setDaysBeforeRemindForWaitingApplications(Integer daysBeforeRemindForWaitingApplications) {
        this.daysBeforeRemindForWaitingApplications = daysBeforeRemindForWaitingApplications;
    }

    public boolean isRemindForUpcomingApplications() {
        return remindForUpcomingApplications;
    }

    public void setRemindForUpcomingApplications(boolean remindForStartingSoonApplications) {
        this.remindForUpcomingApplications = remindForStartingSoonApplications;
    }

    public Integer getDaysBeforeRemindForUpcomingApplications() {
        return daysBeforeRemindForUpcomingApplications;
    }

    public void setDaysBeforeRemindForUpcomingApplications(Integer daysBeforeRemindForUpcomingApplications) {
        this.daysBeforeRemindForUpcomingApplications = daysBeforeRemindForUpcomingApplications;
    }

    public boolean isRemindForUpcomingHolidayReplacement() {
        return remindForUpcomingHolidayReplacement;
    }

    public void setRemindForUpcomingHolidayReplacement(boolean remindForHolidayReplacementApplications) {
        this.remindForUpcomingHolidayReplacement = remindForHolidayReplacementApplications;
    }

    public Integer getDaysBeforeRemindForUpcomingHolidayReplacement() {
        return daysBeforeRemindForUpcomingHolidayReplacement;
    }

    public void setDaysBeforeRemindForUpcomingHolidayReplacement(Integer daysBeforeRemindForUpcomingHolidayReplacement) {
        this.daysBeforeRemindForUpcomingHolidayReplacement = daysBeforeRemindForUpcomingHolidayReplacement;
    }
}
