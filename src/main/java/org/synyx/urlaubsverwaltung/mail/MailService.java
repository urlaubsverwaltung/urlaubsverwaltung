package org.synyx.urlaubsverwaltung.mail;

/**
 * This service provides sending notification emails.
 */
public interface MailService {

    /**
     * Send a mail to the given parameters from the given {@link LegacyMail}
     *
     * @param mail that defines the parameters to send the mail
     */
    void legacySend(LegacyMail mail);
}
