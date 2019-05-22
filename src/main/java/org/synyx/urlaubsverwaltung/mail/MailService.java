
package org.synyx.urlaubsverwaltung.mail;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;

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
     * Sends a technical mail to the defined administrator in the {@link org.synyx.urlaubsverwaltung.settings.MailSettings}
     *
     * @param subjectMessageKey message key of the subject
     * @param templateName name of template
     * @param model additional information based on the template
     */
    void sendTechnicalMail(String subjectMessageKey, String templateName, Map<String, Object> model);


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
     * Sends a mail as reminder to notify about applications for leave waiting longer already to be processed.
     *
     * @param applications waiting to be processed longer already
     */
    void sendRemindForWaitingApplicationsReminderNotification(List<Application> applications);
}
