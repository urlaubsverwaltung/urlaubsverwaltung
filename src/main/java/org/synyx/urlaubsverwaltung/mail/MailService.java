package org.synyx.urlaubsverwaltung.mail;

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
     * @param person            to get this email
     * @param subjectMessageKey message key of the subject
     * @param templateName      name of template
     * @param model             additional information based on the template
     */
    void sendMailTo(Person person, String subjectMessageKey, String templateName, Map<String, Object> model);


    /**
     * Sends a mail to a each person separately
     *
     * @param persons           to get this email each
     * @param subjectMessageKey message key of the subject
     * @param templateName      name of template
     * @param model             additional information based on the template
     * @param args              additional information for subjectMessageKey
     */
    void sendMailToEach(List<Person> persons, String subjectMessageKey, String templateName, Map<String, Object> model, Object... args);


    /**
     * Sends a mail defined by mail notification groups
     *
     * @param mailNotification  group of people to get this email
     * @param subjectMessageKey message key of the subject
     * @param templateName      name of template
     * @param model             additional information based on the template
     */
    void sendMailTo(MailNotification mailNotification, String subjectMessageKey, String templateName, Map<String, Object> model);


    /**
     * Sends a technical mail to the defined administrator in the {@link org.synyx.urlaubsverwaltung.settings.MailSettings}
     *
     * @param subjectMessageKey message key of the subject
     * @param templateName      name of template
     * @param model             additional information based on the template
     */
    void sendTechnicalMail(String subjectMessageKey, String templateName, Map<String, Object> model);
}
