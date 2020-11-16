package org.synyx.urlaubsverwaltung.absence;

import javax.persistence.Embeddable;

/**
 * Settings concerning absence of persons because of vacation or sick days.
 */
@Embeddable
public class AbsenceSettings {

    /**
     * Specifies the maximal number of annual vacation days a person can have.
     */
    private Integer maximumAnnualVacationDays = 40; // NOSONAR

    /**
     * Specifies how many months in advance a person can apply for leave.
     */
    private Integer maximumMonthsToApplyForLeaveInAdvance = 12; // NOSONAR

    /**
     * Activates a notification after {daysBeforeWaitingApplicationsReminderNotification} days for waiting applications
     */
    private boolean remindForWaitingApplications = false;

    /**
     * Specifies when a reminder for waiting application should be send to boss / department head /
     * secondary stage authority
     */
    private Integer daysBeforeRemindForWaitingApplications = 2;

    public Integer getMaximumAnnualVacationDays() {
        return maximumAnnualVacationDays;
    }

    public void setMaximumAnnualVacationDays(Integer maximumAnnualVacationDays) {
        this.maximumAnnualVacationDays = maximumAnnualVacationDays;
    }

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

    public Integer getDaysBeforeRemindForWaitingApplications() {
        return daysBeforeRemindForWaitingApplications;
    }

    public void setDaysBeforeRemindForWaitingApplications(Integer daysBeforeRemindForWaitingApplications) {
        this.daysBeforeRemindForWaitingApplications = daysBeforeRemindForWaitingApplications;
    }
}
