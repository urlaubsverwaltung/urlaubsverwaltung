package org.synyx.urlaubsverwaltung.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class MailSenderService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final JavaMailSender mailSender;

    @Autowired
    MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param from      mail address from where the mail is sent
     * @param recipient mail address where the mail should be sent to
     * @param subject   mail subject
     * @param text      mail body
     */
    void sendEmail(String from, String replyTo, @Nullable String recipient, String subject, String text) {

        if (recipient == null || recipient.isBlank()) {
            LOG.warn("Could not send email to empty recipients!");
            return;
        }

        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setReplyTo(replyTo);
        mailMessage.setTo(recipient);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        try {
            mailSender.send(mailMessage);

            if (LOG.isDebugEnabled()) {
                for (String recipient1 : mailMessage.getTo()) {
                    LOG.debug("Sent email to {}", recipient1);
                }
                LOG.debug("To={}\n\nSubject={}\n\nText={}",
                    Arrays.toString(mailMessage.getTo()), mailMessage.getSubject(), mailMessage.getText());
            }
        } catch (MailException ex) {
            for (String recipient1 : mailMessage.getTo()) {
                LOG.error("Sending email to {} failed", recipient1, ex);
            }
        }
    }

    /**
     * Send a mail with the given subject and text to the given recipients.
     *
     * @param from            mail address from where the mail is sent
     * @param recipient       mail address where the mail should be sent to
     * @param subject         mail subject
     * @param text            mail body
     * @param mailAttachments List of attachments to add to the mail
     */
    void sendEmail(String from, String replyTo, @Nullable String recipient, String subject, String text, List<MailAttachment> mailAttachments) {

        if (recipient == null || recipient.isBlank()) {
            LOG.warn("Could not send email to empty recipients!");
            return;
        }

        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setReplyTo(replyTo);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(text);

            for (MailAttachment mailAttachment : mailAttachments) {
                helper.addAttachment(mailAttachment.getName(), mailAttachment.getContent());
            }
        } catch (MessagingException e) {
            LOG.error("Sending email to {} failed", recipient, e);
        }
        mailSender.send(mimeMessage);
    }
}
