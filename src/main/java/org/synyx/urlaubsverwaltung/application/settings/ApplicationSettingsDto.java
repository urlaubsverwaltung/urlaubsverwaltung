package org.synyx.urlaubsverwaltung.application.settings;

public class ApplicationSettingsDto {

    private Long id;

    private Integer maximumMonthsToApplyForLeaveInAdvance;

    private boolean remindForWaitingApplications;

    private boolean allowHalfDays;

    private Integer daysBeforeRemindForWaitingApplications ;

    private boolean remindForUpcomingApplications;

    private Integer daysBeforeRemindForUpcomingApplications;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
