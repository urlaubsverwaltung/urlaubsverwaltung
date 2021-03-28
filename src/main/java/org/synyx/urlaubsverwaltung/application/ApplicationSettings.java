package org.synyx.urlaubsverwaltung.application;

import javax.persistence.Embeddable;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class ApplicationSettings {

    /**
     * Specifies how many months in advance a person can apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveInAdvance = 12;

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
     * Specifies when a reminder for upcoming application should be send
     */
    private Integer daysBeforeRemindForUpcomingApplications = 3;

    public Integer getMaximumMonthsToApplyForLeaveInAdvance() {
        return maximumMonthsToApplyForLeaveInAdvance;
    }

    public void setMaximumMonthsToApplyForLeaveInAdvance(Integer maximumMonthsToApplyForLeaveInAdvance) {
        this.maximumMonthsToApplyForLeaveInAdvance = maximumMonthsToApplyForLeaveInAdvance;
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
}
