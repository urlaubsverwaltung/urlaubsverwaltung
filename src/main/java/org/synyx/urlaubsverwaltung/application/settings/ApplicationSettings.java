package org.synyx.urlaubsverwaltung.application.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class ApplicationSettings implements Serializable {

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
    @Column(nullable = false)
    private boolean remindForWaitingApplications = false;

    /**
     * Specifies if applications can be done for half-day leave
     */
    @Column(nullable = false)
    private boolean allowHalfDays = true;

    /**
     * Specifies when a reminder for waiting application should be send to boss / department head /
     * secondary stage authority
     */
    private Integer daysBeforeRemindForWaitingApplications = 2;

    /**
     * Activates a notification {daysBeforeRemindForUpcomingApplications} days before upcoming applications
     */
    @Column(nullable = false)
    private boolean remindForUpcomingApplications = false;

    /**
     * Specifies when a reminder for upcoming application should be sent
     */
    private Integer daysBeforeRemindForUpcomingApplications = 3;

    /**
     * Activates a notification after {daysBeforeRemindForUpcomingHolidayReplacement} days for upcoming replacement
     */
    @Column(nullable = false)
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationSettings that = (ApplicationSettings) o;
        return remindForWaitingApplications == that.remindForWaitingApplications
            && allowHalfDays == that.allowHalfDays
            && remindForUpcomingApplications == that.remindForUpcomingApplications
            && remindForUpcomingHolidayReplacement == that.remindForUpcomingHolidayReplacement
            && Objects.equals(maximumMonthsToApplyForLeaveInAdvance, that.maximumMonthsToApplyForLeaveInAdvance)
            && Objects.equals(maximumMonthsToApplyForLeaveAfterwards, that.maximumMonthsToApplyForLeaveAfterwards)
            && Objects.equals(daysBeforeRemindForWaitingApplications, that.daysBeforeRemindForWaitingApplications)
            && Objects.equals(daysBeforeRemindForUpcomingApplications, that.daysBeforeRemindForUpcomingApplications)
            && Objects.equals(daysBeforeRemindForUpcomingHolidayReplacement, that.daysBeforeRemindForUpcomingHolidayReplacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximumMonthsToApplyForLeaveInAdvance, maximumMonthsToApplyForLeaveAfterwards,
            remindForWaitingApplications, allowHalfDays, daysBeforeRemindForWaitingApplications,
            remindForUpcomingApplications, daysBeforeRemindForUpcomingApplications, remindForUpcomingHolidayReplacement,
            daysBeforeRemindForUpcomingHolidayReplacement);
    }
}
