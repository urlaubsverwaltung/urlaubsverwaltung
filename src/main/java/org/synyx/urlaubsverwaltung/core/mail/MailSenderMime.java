package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.pdf.*;


import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Sends mails using {@link JavaMailSenderImpl}.
 *
 * @author  Jan Rosum rosum@synyx.de
 */
@Service
class MailSenderMime {

    private static final Logger LOG = Logger.getLogger(MailSenderMime.class);

    private final JavaMailSenderImpl mailSender;

    @Autowired
    MailSenderMime(@Qualifier("javaMailSender") JavaMailSenderImpl mailSender) {

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
            try {
                MimeMessage mailMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true,"UTF-8");
                pdfMaker pdfAttach = new pdfMaker(25,500);
                PdfDatasourceMaker DatasourceMaker = new PdfDatasourceMaker();

                //SimpleMailMessage mailMessage = new SimpleMailMessage();

                String[] addressTo = new String[recipients.size()];

                for (int i = 0; i < recipients.size(); i++) {
                    addressTo[i] = recipients.get(i);
                }

                try {
                    helper.setFrom(mailSettings.getFrom());
                    helper.setTo(addressTo);
                    helper.setSubject(subject);
                    helper.setText(text);
                    pdfAttach.textEingabe(text,14);
                    helper.addAttachment("Urlaubsantrag.pdf",DatasourceMaker.mimeMaker(pdfAttach.pdfCreator()));
                } catch(IOException|MessagingException mex) {
                    throw new RuntimeException("Error while creating MIME message with PDF Attachment.", mex);
                }
                send(mailMessage, mailSettings);
            } catch (MessagingException mex) {
                throw new RuntimeException(mex);
            }

        }
    }


    private void send(MimeMessage message, MailSettings mailSettings) {
        Address[] recipients = new Address[] {};
        try {
             recipients = message.getAllRecipients();

            if (mailSettings.isActive()) {
                this.mailSender.setHost(mailSettings.getHost());
                this.mailSender.setPort(mailSettings.getPort());
                this.mailSender.setUsername(mailSettings.getUsername());
                this.mailSender.setPassword(mailSettings.getPassword());

                this.mailSender.send(message);


                for (Address recipient : message.getAllRecipients()) {
                    LOG.info("Sent email to " + recipient.toString());
                }
            } else {
                for (Address recipient : message.getAllRecipients()) {
                    LOG.info("No email configuration to send email to " + recipient.toString());
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("To=" + Arrays.toString(recipients) + "\n\n"
                    + "Subject=" + message.getSubject() + "\n\n");

            }
        } catch (MessagingException|MailException ex) {
            if(recipients != null) {
                for (Address recipient : recipients) {
                    LOG.error("Sending email to " + recipient + " failed", ex);
                }
            }
        }
    }
}
