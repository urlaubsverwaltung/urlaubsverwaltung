package org.synyx.urlaubsverwaltung.mail;

/**
 * This service provides sending notification emails.
 */
public interface MailService {

    /**
     * Send a mail to the given parameters from the given {@link Mail}
     *
     * @param mail that defines the parameters to send the mail
     */
    void send(Mail mail);
}
