
package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.calendarintegration.absence.Absence;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;

import java.util.List;
import java.util.Map;


/**
 * This service provides sending notification emails.
 */
public interface MailService {


    /**
     * Sends a mail to a person
     *
     * @param person to get this email
     * @param subjectMessageKey message key of the subject
     * @param templateName name of template
     * @param model additional information based on the template
     */
    void sendMailTo(Person person, String subjectMessageKey, String templateName, Map<String, Object> model);


    /**
     * Sends a mail to a list of persons
     *
     * @param persons to get this email
     * @param subjectMessageKey message key of the subject
     * @param templateName name of template
     * @param model additional information based on the template
     */
    void sendMailTo(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model);


    /**
     * Sends a mail defined by mail notification groups
     *
     * @param mailNotification group of people to get this email
     * @param subjectMessageKey message key of the subject
     * @param templateName name of template
     * @param model additional information based on the template
     */
    void sendMailTo(MailNotification mailNotification, String subjectMessageKey, String templateName, Map<String, Object> model);


    /**
     * Sends an email to the applicant that the application
     * has been made successfully.
     *
     * @param  application confirmed application
     * @param  comment additional comment for the confirming application
     */
    void sendConfirmation(Application application, ApplicationComment comment);


    /**
     * Sends an email to the person of the given application
     * that the office has applied for leave on behalf of himself.
     *
     * @param  application confirmed application on behalf
     * @param  comment additional comment for the application
     */
    void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment);


    /**
     * Sends an email to the bosses notifying
     * that there is a new application for leave
     * which has to be allowed or rejected by a boss.
     *
     * @param  application to allow or reject
     * @param  comment additional comment for the application
     */
    void sendNewApplicationNotification(Application application, ApplicationComment comment);


    /**
     * Sends an email to the applicant and to the second stage authorities that the application for leave has been
     * allowed temporary.
     *
     * @param  application  that has been allowed temporary by a department head
     * @param  comment  contains reason why application for leave has been allowed temporary
     */
    void sendTemporaryAllowedNotification(Application application, ApplicationComment comment);


    /**
     * If an application has status waiting and no boss has decided about it after a certain time, the bosses receive a
     * reminding notification.
     *
     * @param  application to receive a reminding notification
     */
    void sendRemindBossNotification(Application application);


    /**
     * Send an email to the applicant if an application for leave got cancelled by office.
     *
     * @param  application  the application which got cancelled
     * @param  comment  describes the reason of the cancellation
     */
    void sendCancelledByOfficeNotification(Application application, ApplicationComment comment);


    /**
     * Send an email to the tool's manager if an error occurs during adding calendar event.
     *
     * @param  calendarName  that is used for syncing
     * @param  absence  represents the absence of a person
     * @param  exception  describes the error
     */
    void sendCalendarSyncErrorNotification(String calendarName, Absence absence, String exception);


    /**
     * Send an email to the tool's manager if an error occurs during update of calendar event.
     *
     * @param  calendarName  that is used for syncing
     * @param  absence  represents the absence of a person
     * @param  eventId  unique calendar event id
     * @param  exception  describes the error
     */
    void sendCalendarUpdateErrorNotification(String calendarName, Absence absence, String eventId, String exception);


    /**
     * Send an email to the tool's manager if an error occurs during syncing delete action to calendar.
     *
     * @param  calendarName  name of calendar that is used for syncing
     * @param  eventId  id of event which should be deleted
     * @param  exception  describes the error
     */
    void sendCalendarDeleteErrorNotification(String calendarName, String eventId, String exception);


    /**
     * Sends mail to the tool's manager if holidays accounts were updated successfully on 1st January of a year.
     * (setting remaining vacation days)
     *
     * @param  updatedAccounts  that have been successfully updated
     */
    void sendSuccessfullyUpdatedAccountsNotification(List<Account> updatedAccounts);


    /**
     * Sends mail to the tool's manager if settings has been updated to ensure that the mail configuration works.
     *
     * @param settings the updated {@link Settings} to notify via mail
     */
    void sendSuccessfullyUpdatedSettingsNotification(Settings settings);


    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     *
     * @param  sickNote that is about to end
     */
    void sendEndOfSickPayNotification(SickNote sickNote);

    // TODO: To be used as soon as all missing features for database authentication are implemented!
    /**
     * Sends mail to person to inform that his/her account has been created.
     *
     * @param  person  New account owner
     * @param  rawPassword  First time usage password
     */
    void sendUserCreationNotification(Person person, String rawPassword);

    /**
     * Sends a mail as reminder to notify about applications for leave waiting longer already to be processed.
     *
     * @param applications waiting to be processed longer already
     */
    void sendRemindForWaitingApplicationsReminderNotification(List<Application> applications);
}
