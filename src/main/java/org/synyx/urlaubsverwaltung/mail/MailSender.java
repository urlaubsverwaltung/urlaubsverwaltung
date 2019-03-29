package org.synyx.urlaubsverwaltung.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.settings.MailSettings;

import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Sends mails using {@link JavaMailSenderImpl}.
 */
@Service
class MailSender {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JavaMailSenderImpl mailSender;

    @Autowired
    MailSender(@Qualifier("javaMailSender") JavaMailSenderImpl mailSender) {

        this.mailSender = mailSender;
    }

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param  mailSettings  contains settings that should be used to send mails
     * @param  recipients  mail addresses where the mail should be sent to
     * @param  subject  mail subject
     * @param  text  mail body
     */
    void sendEmail(MailSettings mailSettings, List<String> recipients, String subject, String text) {

        if (recipients != null && !recipients.isEmpty()) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            String[] addressTo = new String[recipients.size()];

            for (int i = 0; i < recipients.size(); i++) {
                addressTo[i] = recipients.get(i);
            }

            mailMessage.setFrom(mailSettings.getFrom());
            mailMessage.setTo(addressTo);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            send(mailMessage, mailSettings);
        }
    }


    private void send(SimpleMailMessage message, MailSettings mailSettings) {

        try {
            if (mailSettings.isActive()) {
                this.mailSender.setHost(mailSettings.getHost());
                this.mailSender.setPort(mailSettings.getPort());
                this.mailSender.setUsername(mailSettings.getUsername());
                this.mailSender.setPassword(mailSettings.getPassword());

                this.mailSender.send(message);

                for (String recipient : message.getTo()) {
                    LOG.info("Sent email to {}", recipient);
                }
            } else {
                for (String recipient : message.getTo()) {
                    LOG.info("No email configuration to send email to {}", recipient);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("To={}\n\nSubject={}\n\nText={}",
                        Arrays.toString(message.getTo()), message.getSubject(), message.getText());
            }
        } catch (MailException ex) {
            for (String recipient : message.getTo()) {
                LOG.error("Sending email to {} failed", recipient, ex);
            }
        }
    }
}
