package org.synyx.urlaubsverwaltung.core.mail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class MailSenderTest {

    private MailSender mailSender;

    private JavaMailSenderImpl javaMailSender;
    private Settings settings;

    @Before
    public void setUp() throws Exception {

        javaMailSender = Mockito.mock(JavaMailSenderImpl.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);
        mailSender = new MailSender(javaMailSender);

        settings = new Settings();
        settings.getMailSettings().setActive(true);

        when(settingsService.getSettings()).thenReturn(settings);
    }


    @Test
    public void ensureNoMailSentIfSendingMailsIsDeactivated() {

        settings.getMailSettings().setActive(false);

        mailSender.sendEmail(settings.getMailSettings(), Collections.singletonList("foo@bar.de"), "subject", "text");

        Mockito.verifyZeroInteractions(javaMailSender);
    }


    @Test
    public void ensureMailSenderAttributesAreUpdatedWhenSendingMails() {

        MailSettings mailSettings = settings.getMailSettings();

        mailSender.sendEmail(mailSettings, Collections.singletonList("foo@bar.de"), "subject", "text");

        verify(javaMailSender).setHost(mailSettings.getHost());
        verify(javaMailSender).setPort(mailSettings.getPort());
        verify(javaMailSender).setUsername(mailSettings.getUsername());
        verify(javaMailSender).setPassword(mailSettings.getPassword());
    }


    @Test
    public void ensureMailIsSentCorrectly() {

        MailSettings mailSettings = settings.getMailSettings();

        ArgumentCaptor<SimpleMailMessage> mailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        String subject = "subject";
        String body = "text";

        mailSender.sendEmail(mailSettings, Arrays.asList("max@firma.test", "marlene@firma.test"), subject, body);

        verify(javaMailSender).send(mailMessageArgumentCaptor.capture());

        SimpleMailMessage mailMessage = mailMessageArgumentCaptor.getValue();

        Assert.assertNotNull("There must be recipients", mailMessage.getTo());
        Assert.assertEquals("Wrong number of recipients", 2, mailMessage.getTo().length);
        Assert.assertEquals("Wrong subject", subject, mailMessage.getSubject());
        Assert.assertEquals("Wrong body", body, mailMessage.getText());
    }


    @Test
    public void ensureNoMailIsSentIfRecipientsListIsNull() {

        MailSettings mailSettings = settings.getMailSettings();

        mailSender.sendEmail(mailSettings, null, "subject", "text");

        Mockito.verifyZeroInteractions(javaMailSender);
    }


    @Test
    public void ensureNoMailIsSentIfRecipientsListIsEmpty() {

        MailSettings mailSettings = settings.getMailSettings();

        mailSender.sendEmail(mailSettings, Collections.emptyList(), "subject", "text");

        Mockito.verifyZeroInteractions(javaMailSender);
    }
}
