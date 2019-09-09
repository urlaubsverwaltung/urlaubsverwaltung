package org.synyx.urlaubsverwaltung.mail;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Service
public class SpringBootConfiguredMailSender implements MailSender {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JavaMailSender javaMailSender;

    @Autowired
    SpringBootConfiguredMailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String from, List<String> recipients, String subject, String text) {

        if(recipients == null || recipients.isEmpty()) {
            LOG.warn("Could not send email to empty recipients!");
            return;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(recipients.toArray(new String[0]));
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        send(mailMessage);
    }

    private void send(SimpleMailMessage message) {
        try {

            this.javaMailSender.send(message);

            for (String recipient : message.getTo()) {
                LOG.info("Sent email to {}", recipient);
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
