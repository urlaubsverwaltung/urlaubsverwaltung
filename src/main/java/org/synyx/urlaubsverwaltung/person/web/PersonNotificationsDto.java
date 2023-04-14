package org.synyx.urlaubsverwaltung.person.web;

public class PersonNotificationsDto {

    private Integer personId;

    private boolean all;

    private PersonNotificationDto applicationManagementAll;
    private PersonNotificationDto applicationAppliedAndChangesForManagement;
    private PersonNotificationDto applicationWaitingReminderForManagement;
    private PersonNotificationDto applicationCancellationRequestedForManagement;
    private PersonNotificationDto applicationAppliedAndChanges;
    private PersonNotificationDto applicationUpcoming;
    private PersonNotificationDto holidayReplacement;
    private PersonNotificationDto holidayReplacementUpcoming;
    private PersonNotificationDto personNewManagementAll;
    private PersonNotificationDto overtimeManagementAll;

    PersonNotificationsDto() {
        // ok
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public PersonNotificationDto getApplicationManagementAll() {
        return applicationManagementAll;
    }

    public void setApplicationManagementAll(PersonNotificationDto applicationManagementAll) {
        this.applicationManagementAll = applicationManagementAll;
    }

    public PersonNotificationDto getApplicationAppliedAndChangesForManagement() {
        return applicationAppliedAndChangesForManagement;
    }

    public void setApplicationAppliedAndChangesForManagement(PersonNotificationDto applicationAppliedAndChangesForManagement) {
        this.applicationAppliedAndChangesForManagement = applicationAppliedAndChangesForManagement;
    }

    public PersonNotificationDto getApplicationWaitingReminderForManagement() {
        return applicationWaitingReminderForManagement;
    }

    public void setApplicationWaitingReminderForManagement(PersonNotificationDto applicationWaitingReminderForManagement) {
        this.applicationWaitingReminderForManagement = applicationWaitingReminderForManagement;
    }

    public PersonNotificationDto getApplicationCancellationRequestedForManagement() {
        return applicationCancellationRequestedForManagement;
    }

    public void setApplicationCancellationRequestedForManagement(PersonNotificationDto applicationCancellationRequestedForManagement) {
        this.applicationCancellationRequestedForManagement = applicationCancellationRequestedForManagement;
    }

    public PersonNotificationDto getApplicationAppliedAndChanges() {
        return applicationAppliedAndChanges;
    }

    public void setApplicationAppliedAndChanges(PersonNotificationDto applicationAppliedAndChanges) {
        this.applicationAppliedAndChanges = applicationAppliedAndChanges;
    }

    public PersonNotificationDto getApplicationUpcoming() {
        return applicationUpcoming;
    }

    public void setApplicationUpcoming(PersonNotificationDto applicationUpcoming) {
        this.applicationUpcoming = applicationUpcoming;
    }

    public PersonNotificationDto getHolidayReplacement() {
        return holidayReplacement;
    }

    public void setHolidayReplacement(PersonNotificationDto holidayReplacement) {
        this.holidayReplacement = holidayReplacement;
    }

    public PersonNotificationDto getHolidayReplacementUpcoming() {
        return holidayReplacementUpcoming;
    }

    public void setHolidayReplacementUpcoming(PersonNotificationDto holidayReplacementUpcoming) {
        this.holidayReplacementUpcoming = holidayReplacementUpcoming;
    }

    public PersonNotificationDto getPersonNewManagementAll() {
        return personNewManagementAll;
    }

    public void setPersonNewManagementAll(PersonNotificationDto personNewManagementAll) {
        this.personNewManagementAll = personNewManagementAll;
    }

    public PersonNotificationDto getOvertimeManagementAll() {
        return overtimeManagementAll;
    }

    public void setOvertimeManagementAll(PersonNotificationDto overtimeManagementAll) {
        this.overtimeManagementAll = overtimeManagementAll;
    }
}
