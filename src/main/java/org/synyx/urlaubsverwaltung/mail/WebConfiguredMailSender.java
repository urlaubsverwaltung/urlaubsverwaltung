package org.synyx.urlaubsverwaltung.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * This MailSender is configured via the settings page.
 *
 * @deprecated will be removed in version 3.0 - configure mail settings via application.properties
 */
@Deprecated
public class WebConfiguredMailSender implements MailSender {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JavaMailSenderImpl mailSender;
    private final SettingsService settingsService;

    @Autowired
    public WebConfiguredMailSender(JavaMailSenderImpl mailSender,
                                   SettingsService settingsService) {

        this.mailSender = mailSender;
        this.settingsService = settingsService;
    }


    public void sendEmail(String from, List<String> recipients, String subject, String text) {

        if (recipients != null && !recipients.isEmpty()) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            String[] addressTo = new String[recipients.size()];

            for (int i = 0; i < recipients.size(); i++) {
                addressTo[i] = recipients.get(i);
            }

            mailMessage.setFrom(from);
            mailMessage.setTo(addressTo);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);

            send(mailMessage);
        }
    }

    private void send(SimpleMailMessage message) {

        MailSettings mailSettings = settingsService.getSettings().getMailSettings();

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
