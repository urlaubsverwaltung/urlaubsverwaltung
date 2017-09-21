
package org.synyx.urlaubsverwaltung.core.mail;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.overtime.Overtime;
import org.synyx.urlaubsverwaltung.core.overtime.OvertimeComment;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;

import java.util.List;


/**
 * This service provides sending notification emails.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

public interface MailService {

    /**
     * sends an email to the applicant that the application has been made successfully.
     *
     * @param  application
     * @param  comment
     */
    void sendConfirmation(Application application, ApplicationComment comment);


    /**
     * sends an email to the person of the given application that the office has applied for leave on behalf of himself.
     *
     * @param  application
     * @param  comment
     */
    void sendAppliedForLeaveByOfficeNotification(Application application, ApplicationComment comment);


    /**
     * sends an email to the bosses notifying that there is a new application for leave which has to be allowed or
     * rejected by a boss.
     *
     * @param  application
     * @param  comment
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
     * /** Sends an email to the applicant and to the office that the application for leave has been allowed.
     *
     * @param  application  that has been allowed by a privileged user
     * @param  comment  contains reason why application for leave has been allowed
     */
    void sendAllowedNotification(Application application, ApplicationComment comment);


    /**
     * sends an email to the applicant that the application has been rejected.
     *
     * @param  application  the application which got rejected
     * @param  comment  reason why application was rejected
     */
    void sendRejectedNotification(Application application, ApplicationComment comment);


    /**
     * If a boss is not sure about the decision of an application (reject or allow), he can ask another boss to decide
     * about this application via a generated email.
     *
     * @param  application
     * @param  recipient
     * @param  sender
     */
    void sendReferApplicationNotification(Application application, Person recipient, Person sender);


    /**
     * If an application has status waiting and no boss has decided about it after a certain time, the bosses receive a
     * reminding notification.
     *
     * @param  application
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
     */
    void sendSuccessfullyUpdatedSettingsNotification(Settings settings);


    /**
     * Sends mail to the affected person if sick note is converted to vacation.
     *
     * @param  application
     */
    void sendSickNoteConvertedToVacationNotification(Application application);


    /**
     * Sends mail to person and office if sick pay (gesetzliche Lohnfortzahlung im Krankheitsfall) is about to end.
     *
     * @param  sickNote
     */
    void sendEndOfSickPayNotification(SickNote sickNote);


    /**
     * Sends mail to person to inform that he/she has been selected as holiday replacement that stands in while someone
     * is on holiday.
     *
     * @param  application
     */
    void notifyHolidayReplacement(Application application);


    // TODO: To be used as soon as all missing features for database authentication are implemented!
    /**
     * Sends mail to person to inform that his/her account has been created.
     *
     * @param  person  New account owner
     * @param  rawPassword  First time usage password
     */
    void sendUserCreationNotification(Person person, String rawPassword);


    /**
     * Sends mail to office and informs about a cancellation request of an already allowed application.
     *
     * @param  application
     * @param  createdComment
     */
    void sendCancellationRequest(Application application, ApplicationComment createdComment);


    /**
     * Sends a mail to the office after someone added an overtime record.
     *
     * @param  overtime  that has been added
     * @param  overtimeComment  may contain further information
     */
    void sendOvertimeNotification(Overtime overtime, OvertimeComment overtimeComment);

    /**
     * Sends a mail as reminder to notify about applications for leave waiting longer already to be processed.
     *
     * @param applications waiting to be processed longer already
     */
    void sendRemindForWaitingApplicationsReminderNotification(List<Application> applications);
}
