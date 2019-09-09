package org.synyx.urlaubsverwaltung.mail;

import java.util.List;


public interface MailSender {

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param  from  mail address from where the mail is sent
     * @param  recipients  mail addresses where the mail should be sent to
     * @param  subject  mail subject
     * @param  text  mail body
     */
    void sendEmail(String from, List<String> recipients, String subject, String text);
}
